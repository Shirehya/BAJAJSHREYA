package com.bajajfinserv.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@SpringBootApplication
public class BajajFinservChallengeApplication implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        SpringApplication.run(BajajFinservChallengeApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // Step 1: Generate webhook
            WebhookRequest webhookRequest = new WebhookRequest("Shirehya KP", "22BDS0365", "shirehya.kp2022@vitstudent.ac.in");
            WebhookResponse webhookResponse = generateWebhook(webhookRequest);
            
            System.out.println("Webhook generated successfully!");
            System.out.println("Webhook URL: " + webhookResponse.getWebhook());
            
            // Step 2: Solve SQL problem (Question 1 - since regNo ends with 47, which is odd)
            String finalQuery = getSqlSolution();
            
            // Step 3: Submit solution
            submitSolution(webhookResponse.getWebhook(), webhookResponse.getAccessToken(), finalQuery);
            
            System.out.println("Solution submitted successfully!");
            
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private WebhookResponse generateWebhook(WebhookRequest request) throws Exception {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<WebhookRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        
        return new WebhookResponse(
            jsonNode.get("webhook").asText(),
            jsonNode.get("accessToken").asText()
        );
    }

    private String getSqlSolution() {
        // Final SQL query for Question 1
        return "SELECT p.AMOUNT as SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) as NAME, " +
               "YEAR(CURDATE()) - YEAR(e.DOB) - (DATE_FORMAT(CURDATE(), '%m%d') < DATE_FORMAT(e.DOB, '%m%d')) as AGE, " +
               "d.DEPARTMENT_NAME FROM PAYMENTS p JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
               "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID WHERE DAY(p.PAYMENT_TIME) != 1 " +
               "AND p.AMOUNT = (SELECT MAX(AMOUNT) FROM PAYMENTS WHERE DAY(PAYMENT_TIME) != 1);";
    }

    private void submitSolution(String webhookUrl, String accessToken, String finalQuery) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        
        SolutionRequest solutionRequest = new SolutionRequest(finalQuery);
        HttpEntity<SolutionRequest> entity = new HttpEntity<>(solutionRequest, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(webhookUrl, HttpMethod.POST, entity, String.class);
        
        System.out.println("Response: " + response.getBody());
    }

    // Inner classes for request/response objects
    static class WebhookRequest {
        private String name;
        private String regNo;
        private String email;

        public WebhookRequest(String name, String regNo, String email) {
            this.name = name;
            this.regNo = regNo;
            this.email = email;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRegNo() { return regNo; }
        public void setRegNo(String regNo) { this.regNo = regNo; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    static class WebhookResponse {
        private String webhook;
        private String accessToken;

        public WebhookResponse(String webhook, String accessToken) {
            this.webhook = webhook;
            this.accessToken = accessToken;
        }

        // Getters and setters
        public String getWebhook() { return webhook; }
        public void setWebhook(String webhook) { this.webhook = webhook; }
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    }

    static class SolutionRequest {
        private String finalQuery;

        public SolutionRequest(String finalQuery) {
            this.finalQuery = finalQuery;
        }

        // Getters and setters
        public String getFinalQuery() { return finalQuery; }
        public void setFinalQuery(String finalQuery) { this.finalQuery = finalQuery; }
    }
}
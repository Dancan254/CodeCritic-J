package com.codecritic.controller;

import com.codecritic.model.GitHubWebhookEvent;
import com.codecritic.service.GitHubWebhookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Formatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    
    @Value("${github.webhook.secret}")
    private String webhookSecret;
    
    private final GitHubWebhookService githubWebhookService;
    
    public WebhookController(GitHubWebhookService githubWebhookService) {
        this.githubWebhookService = githubWebhookService;
    }
    
    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(
            @RequestBody String payload,
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestHeader("X-GitHub-Delivery") String deliveryId) {
        
        logger.info("Received GitHub webhook: {}, delivery: {}", eventType, deliveryId);
        
        // Validate webhook signature
        if (!isValidSignature(payload, signature)) {
            logger.warn("Invalid webhook signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }
        
        // For pull request events, trigger the processing
        if ("pull_request".equals(eventType)) {
            try {
                // Extract PR ID from payload
                // This is a simplified example - in a real app, you'd parse the JSON properly
                long prId = extractPullRequestId(payload);
                String repoName = extractRepositoryName(payload);
                
                // Create and process the webhook event
                GitHubWebhookEvent event = GitHubWebhookEvent.builder()
                        .eventId(deliveryId)
                        .eventType(eventType)
                        .repositoryName(repoName)
                        .pullRequestId(prId)
                        .timestamp(Instant.now())
                        .rawPayload(payload)
                        .build();
                
                // Process the event asynchronously
                githubWebhookService.processWebhookEvent(event);
                
                return ResponseEntity.ok("Webhook received and processing started");
            } catch (Exception e) {
                logger.error("Error processing webhook: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error processing webhook");
            }
        }
        
        // For other event types, acknowledge but don't process
        return ResponseEntity.ok("Webhook received");
    }
    
    /**
     * Validate the webhook signature
     */
    private boolean isValidSignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            try (Formatter formatter = new Formatter(hexString)) {
                for (byte b : digest) {
                    formatter.format("%02x", b);
                }
            }
            
            // GitHub signature starts with "sha256="
            String expectedSignature = "sha256=" + hexString;
            
            // Debug info
            logger.info("Secret used: {}", webhookSecret);
            logger.info("Payload length: {}", payload.length());
            logger.info("Expected signature: {}", expectedSignature);
            logger.info("Received signature: {}", signature);
            
            return expectedSignature.equals(signature);
            
        } catch (Exception e) {
            logger.error("Error validating signature: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Extract pull request ID from the webhook payload
     * This is a simplified implementation - in a real app, you'd use a JSON parser
     */
    private long extractPullRequestId(String payload) {
        // Simple string extraction for demo purposes
        int idIndex = payload.indexOf("\"number\":");
        if (idIndex >= 0) {
            int start = idIndex + 9; // Length of "\"number\":"
            int end = payload.indexOf(",", start);
            return Long.parseLong(payload.substring(start, end).trim());
        }
        return 0;
    }
    
    /**
     * Extract repository name from the webhook payload
     * This is a simplified implementation - in a real app, you'd use a JSON parser
     */
    private String extractRepositoryName(String payload) {
        // Simple string extraction for demo purposes
        int fullNameIndex = payload.indexOf("\"full_name\":");
        if (fullNameIndex >= 0) {
            int start = fullNameIndex + 13; // Length of "\"full_name\":"
            int end = payload.indexOf("\"", start + 1);
            return payload.substring(start, end).trim().replace("\"", "");
        }
        return "unknown/repo";
    }
} 
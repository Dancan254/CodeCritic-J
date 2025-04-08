package com.codecritic.controller;

import com.codecritic.model.GitHubWebhookEvent;
import com.codecritic.service.GitHubWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test", description = "Test endpoints for development purposes")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    private final GitHubWebhookService githubWebhookService;
    
    public TestController(GitHubWebhookService githubWebhookService) {
        this.githubWebhookService = githubWebhookService;
    }
    
    @Operation(summary = "Process a test GitHub webhook event", 
            description = "Simulates a GitHub webhook event for testing without signature validation")
    @PostMapping("/webhook/github")
    public ResponseEntity<String> simulateGitHubWebhook(@RequestBody String payload) {
        logger.info("Received test GitHub webhook simulation");
        
        try {
            // Extract PR ID from payload
            long prId = extractPullRequestId(payload);
            String repoName = extractRepositoryName(payload);
            
            // Create and process the webhook event
            GitHubWebhookEvent event = GitHubWebhookEvent.builder()
                    .eventId("test-" + System.currentTimeMillis())
                    .eventType("pull_request")
                    .repositoryName(repoName)
                    .pullRequestId(prId)
                    .timestamp(Instant.now())
                    .rawPayload(payload)
                    .build();
            
            // Process the event asynchronously
            githubWebhookService.processWebhookEvent(event);
            
            return ResponseEntity.ok("Test webhook received and processing started");
        } catch (Exception e) {
            logger.error("Error processing test webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error processing test webhook: " + e.getMessage());
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
        return 123; // Default test PR ID
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
        return "test/CodeCritic-J"; // Default test repo
    }
} 
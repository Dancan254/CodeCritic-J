package com.codecritic.service;

import com.codecritic.model.ModifiedFile;
import com.codecritic.model.AIReview;
import com.codecritic.model.AgentSession;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Service
public class AIReviewService {

    private static final Logger logger = LoggerFactory.getLogger(AIReviewService.class);
    
    private final OpenAIClient openAIClient;
    private final String deploymentId;
    
    // Prompt template for code review
    private static final String CODE_REVIEW_PROMPT = 
        "You are CodeCritic, an expert Java code reviewer with deep knowledge of best practices, " +
        "design patterns, and performance optimization. Review the following Java code diff and " +
        "provide constructive feedback.";
    
    public AIReviewService(OpenAIClient openAIClient, String aiFoundryDeploymentId) {
        this.openAIClient = openAIClient;
        this.deploymentId = aiFoundryDeploymentId;
    }
    
    /**
     * Generate an AI review for a modified file
     */
    public AIReview generateReview(ModifiedFile file) {
        logger.info("Generating AI review for file: {}", file.getFileName());
        logger.info("Using Azure AI Foundry deployment ID: {}", deploymentId);
        
        try {
            // Skip if it's not a Java file
            if (!file.getFilePath().endsWith(".java")) {
                return createEmptyReview("Skipping non-Java file");
            }
            
            // Skip deleted files
            if (file.getChangeType() == ModifiedFile.ChangeType.DELETED) {
                return createEmptyReview("Skipping deleted file");
            }
            
            // Skip if no diff content
            if (file.getDiffContent() == null || file.getDiffContent().isEmpty()) {
                return createEmptyReview("No code changes to review");
            }
            
            // Create a session for this review
            AgentSession session = AgentSession.builder()
                    .sessionId(UUID.randomUUID().toString())
                    .associatedPrId(0L) // Will be filled in by PR service
                    .createdAt(Instant.now())
                    .lastUpdated(Instant.now())
                    .build();
            
            // Prepare the Azure AI chat messages
            ChatRequestSystemMessage systemMessage = new ChatRequestSystemMessage(CODE_REVIEW_PROMPT);
            
            String userPrompt = "Here is the Java code to review:\n\n```java\n" + 
                                file.getDiffContent() + 
                                "\n```\n\nPlease analyze for:\n" +
                                "1. Code quality and maintainability\n" +
                                "2. Potential bugs or edge cases\n" +
                                "3. Performance issues\n" +
                                "4. Security vulnerabilities\n" +
                                "5. Adherence to Java best practices\n\n" +
                                "Format your response with section headers and bullet points as appropriate.";
            
            ChatRequestUserMessage userMessage = new ChatRequestUserMessage(userPrompt);
            
            ChatCompletionsOptions options = new ChatCompletionsOptions(
                    Arrays.asList(systemMessage, userMessage));
            
            logger.info("Calling Azure AI Foundry API for code review analysis...");
            
            // Generate the AI review
            ChatCompletions chatCompletions = openAIClient.getChatCompletions(
                    deploymentId, options);
            
            String aiResponse = chatCompletions.getChoices().get(0).getMessage().getContent();
            
            logger.info("Received response from Azure AI Foundry - response length: {} characters", 
                    aiResponse.length());
            
            // Create and return the AI review
            return AIReview.builder()
                    .reviewId(UUID.randomUUID().toString())
                    .generatedAt(Instant.now())
                    .aiFeedback(formatAIResponse(file.getFileName(), aiResponse))
                    .contextData("File: " + file.getFilePath())
                    .build();
            
        } catch (Exception e) {
            logger.error("Error generating AI review: {}", e.getMessage(), e);
            return createEmptyReview("Error generating review: " + e.getMessage());
        }
    }
    
    /**
     * Format the AI response with a header for the file
     */
    private String formatAIResponse(String fileName, String aiResponse) {
        return String.format("### %s\n\n%s", fileName, aiResponse);
    }
    
    /**
     * Create an empty review with a message
     */
    protected AIReview createEmptyReview(String message) {
        return AIReview.builder()
                .reviewId(UUID.randomUUID().toString())
                .generatedAt(Instant.now())
                .aiFeedback(message)
                .contextData("Empty review")
                .build();
    }
} 
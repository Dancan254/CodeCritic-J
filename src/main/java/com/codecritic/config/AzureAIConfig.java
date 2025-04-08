package com.codecritic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Configuration for Azure AI Foundry using the Azure SDK
 */
@Configuration
public class AzureAIConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureAIConfig.class);
    
    @Value("${azure.ai.foundry.endpoint}")
    private String endpoint;
    
    @Value("${azure.ai.foundry.key}")
    private String key;
    
    @Value("${azure.ai.foundry.deployment-id}")
    private String deploymentId;
    
    /**
     * Provides an Azure OpenAI client configured for Azure AI Foundry
     */
    @Bean
    public OpenAIClient openAIClient() {
        logger.info("Initializing Azure AI Foundry client with endpoint: {}", endpoint);
        
        return new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(key))
                .buildClient();
    }
    
    /**
     * @return The deployment ID for the AI Foundry model
     */
    @Bean
    public String aiFoundryDeploymentId() {
        return deploymentId;
    }
} 
package com.codecritic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:.env", ignoreResourceNotFound = true)
public class LangChain4jConfig {

    private static final Logger logger = LoggerFactory.getLogger(LangChain4jConfig.class);

    @Value("${langchain4j.azure-ai-foundry.api-key}")
    private String apiKey;
    
    @Value("${langchain4j.azure-ai-foundry.endpoint}")
    private String endpoint;
    
    @Value("${langchain4j.azure-ai-foundry.deployment-id}")
    private String deploymentId;
    
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        logger.info("Configuring Azure AI Foundry chat model with endpoint: {}", endpoint);
        
        // Using OpenAI model with custom baseUrl for Azure AI Foundry
        // This is a workaround until LangChain4j adds direct Azure AI Foundry support
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(endpoint)
                .modelName(deploymentId)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
} 
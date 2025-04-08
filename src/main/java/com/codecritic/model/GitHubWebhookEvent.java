package com.codecritic.model;

import lombok.Data;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubWebhookEvent {
    private String eventId;
    private String eventType;
    private String repositoryName;
    private Long pullRequestId;
    private Instant timestamp;
    private String rawPayload;
} 
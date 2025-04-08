package com.codecritic.model;

import lombok.Data;
import lombok.Builder;
import java.time.Instant;

@Data
@Builder
public class AgentSession {
    private String sessionId;
    private Long associatedPrId;
    private Instant createdAt;
    private Instant lastUpdated;
} 
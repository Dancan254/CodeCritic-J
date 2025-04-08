package com.codecritic.model;

import lombok.Data;
import lombok.Builder;
import java.time.Instant;

@Data
@Builder
public class AIReview {
    private String reviewId;
    private Instant generatedAt;
    private String aiFeedback;
    private String contextData;
} 
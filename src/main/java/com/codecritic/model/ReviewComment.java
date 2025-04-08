package com.codecritic.model;

import lombok.Data;
import lombok.Builder;
import java.time.Instant;

@Data
@Builder
public class ReviewComment {
    private String commentId;
    private String content;
    private Instant createdAt;
} 
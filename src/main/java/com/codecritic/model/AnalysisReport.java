package com.codecritic.model;

import lombok.Data;
import lombok.Builder;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class AnalysisReport {
    private String reportId;
    private Instant generatedAt;
    private String fileId;
    private List<AnalysisIssue> issues;
} 
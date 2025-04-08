package com.codecritic.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class AnalysisIssue {
    private String issueId;
    private String description;
    private Severity severity;
    private int lineNumber;
    private Integer columnNumber;
    
    public enum Severity {
        LOW,
        MEDIUM,
        HIGH
    }
} 
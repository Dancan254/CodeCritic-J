package com.codecritic.model;

import lombok.Data;
import lombok.Builder;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class PullRequest {
    private Long prId;
    private String title;
    private String author;
    private String repository;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ModifiedFile> modifiedFiles;
} 
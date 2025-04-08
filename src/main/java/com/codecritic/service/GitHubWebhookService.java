package com.codecritic.service;

import com.codecritic.model.GitHubWebhookEvent;
import com.codecritic.model.PullRequest;
import com.codecritic.model.ModifiedFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitHubWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubWebhookService.class);
    
    @Value("${github.api.token}")
    private String githubToken;
    
    private final PRAnalysisService prAnalysisService;
    private final GitHubService gitHubService;
    
    public GitHubWebhookService(PRAnalysisService prAnalysisService, GitHubService gitHubService) {
        this.prAnalysisService = prAnalysisService;
        this.gitHubService = gitHubService;
    }
    
    /**
     * Process a GitHub webhook event
     */
    @Async
    public void processWebhookEvent(GitHubWebhookEvent event) {
        logger.info("Processing webhook event: {}", event.getEventType());
        
        if ("pull_request".equals(event.getEventType())) {
            try {
                // Use the GitHubService to fetch pull request details
                PullRequest pullRequest = gitHubService.fetchPullRequestDetails(
                        event.getRepositoryName(), 
                        event.getPullRequestId());
                
                prAnalysisService.analyzePullRequest(pullRequest);
            } catch (Exception e) {
                logger.error("Error processing pull request webhook: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Fetch details about a pull request using the GitHub API
     */
    private PullRequest fetchPullRequestDetails(GitHubWebhookEvent event) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(githubToken).build();
        GHPullRequest ghPullRequest = github.getRepository(event.getRepositoryName())
                .getPullRequest(event.getPullRequestId().intValue());
        
        List<ModifiedFile> modifiedFiles = ghPullRequest.listFiles().toList().stream()
                .filter(file -> file.getFilename().endsWith(".java"))
                .map(this::mapToModifiedFile)
                .collect(Collectors.toList());
        
        return PullRequest.builder()
                .prId(event.getPullRequestId())
                .title(ghPullRequest.getTitle())
                .author(ghPullRequest.getUser().getLogin())
                .createdAt(ghPullRequest.getCreatedAt().toInstant())
                .updatedAt(Instant.now())
                .modifiedFiles(modifiedFiles)
                .build();
    }
    
    /**
     * Map GitHub's file representation to our domain model
     */
    private ModifiedFile mapToModifiedFile(GHPullRequestFileDetail fileDetail) {
        ModifiedFile.ChangeType changeType;
        
        switch (fileDetail.getStatus()) {
            case "added":
                changeType = ModifiedFile.ChangeType.ADDED;
                break;
            case "removed":
                changeType = ModifiedFile.ChangeType.DELETED;
                break;
            default:
                changeType = ModifiedFile.ChangeType.MODIFIED;
        }
        
        return ModifiedFile.builder()
                .fileId(fileDetail.getSha())
                .fileName(getFileNameFromPath(fileDetail.getFilename()))
                .filePath(fileDetail.getFilename())
                .changeType(changeType)
                .diffContent(fileDetail.getPatch())
                .build();
    }
    
    /**
     * Extract the file name from a full path
     */
    private String getFileNameFromPath(String filePath) {
        int lastSlashIndex = filePath.lastIndexOf('/');
        return lastSlashIndex >= 0 ? filePath.substring(lastSlashIndex + 1) : filePath;
    }
} 
package com.codecritic.service;

import com.codecritic.model.PullRequest;
import com.codecritic.model.ReviewComment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHRepository;

import java.io.IOException;

@Service
public class GitHubCommentService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubCommentService.class);
    
    @Value("${github.api.token}")
    private String githubToken;
    
    /**
     * Post a review comment to a GitHub pull request
     */
    public void postComment(PullRequest pullRequest, ReviewComment comment) {
        logger.info("Posting review comment to PR #{}", pullRequest.getPrId());
        
        try {
            // Connect to GitHub API
            GitHub github = new GitHubBuilder().withOAuthToken(githubToken).build();
            
            // Extract repository name from PR context
            // For a real implementation, you would need to store or extract this information
            String repoName = getRepositoryName(pullRequest);
            
            logger.info("Using repository: {}", repoName);
            GHRepository repository = github.getRepository(repoName);
            
            // Get the PR and post the comment
            GHPullRequest pr = repository.getPullRequest(pullRequest.getPrId().intValue());
            
            GHIssueComment postedComment = pr.comment(comment.getContent());
            
            logger.info("Successfully posted comment with ID: {}", postedComment.getId());
            
        } catch (IOException e) {
            logger.error("Error posting comment to GitHub: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Extract repository name from pull request
     * In a real implementation, this would come from your database or PR context
     */
    private String getRepositoryName(PullRequest pullRequest) {
        // Use the repository field from the PullRequest model
        return pullRequest.getRepository();
    }
} 
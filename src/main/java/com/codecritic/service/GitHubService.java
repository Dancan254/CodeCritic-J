package com.codecritic.service;

import com.codecritic.model.PullRequest;

/**
 * Interface for GitHub operations
 */
public interface GitHubService {
    
    /**
     * Fetch pull request details from GitHub
     * 
     * @param repoName Repository name (format: owner/repo)
     * @param prId Pull request ID
     * @return Pull request details
     */
    PullRequest fetchPullRequestDetails(String repoName, long prId);
    
    /**
     * Post a comment to a pull request
     * 
     * @param pullRequest Pull request to comment on
     * @param commentBody Comment text
     */
    void postComment(PullRequest pullRequest, String commentBody);
    
    /**
     * Post a review comment to a specific line in a file
     * 
     * @param pullRequest Pull request to comment on
     * @param filePath Path to the file
     * @param lineNumber Line number to comment on
     * @param commentBody Comment text
     */
    void postReviewComment(PullRequest pullRequest, String filePath, int lineNumber, String commentBody);
} 
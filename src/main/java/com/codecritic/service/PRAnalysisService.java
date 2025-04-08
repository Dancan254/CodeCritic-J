package com.codecritic.service;

import com.codecritic.model.PullRequest;
import com.codecritic.model.ModifiedFile;
import com.codecritic.model.AnalysisReport;
import com.codecritic.model.AnalysisIssue;
import com.codecritic.model.AIReview;
import com.codecritic.model.ReviewComment;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
public class PRAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(PRAnalysisService.class);
    
    private final StaticAnalysisService staticAnalysisService;
    private final AIReviewService aiReviewService;
    private final GitHubCommentService gitHubCommentService;
    
    public PRAnalysisService(
            StaticAnalysisService staticAnalysisService,
            AIReviewService aiReviewService,
            GitHubCommentService gitHubCommentService) {
        this.staticAnalysisService = staticAnalysisService;
        this.aiReviewService = aiReviewService;
        this.gitHubCommentService = gitHubCommentService;
    }
    
    /**
     * Analyze a pull request by running static analysis and AI review
     */
    public void analyzePullRequest(PullRequest pullRequest) {
        logger.info("Analyzing pull request: {} by {}", pullRequest.getTitle(), pullRequest.getAuthor());
        
        List<CompletableFuture<AnalysisReport>> staticAnalysisFutures = new ArrayList<>();
        List<CompletableFuture<AIReview>> aiReviewFutures = new ArrayList<>();
        
        // Process each modified file
        for (ModifiedFile file : pullRequest.getModifiedFiles()) {
            // Skip deleted files
            if (file.getChangeType() == ModifiedFile.ChangeType.DELETED) {
                continue;
            }
            
            // Run static analysis
            CompletableFuture<AnalysisReport> staticAnalysisFuture = 
                    CompletableFuture.supplyAsync(() -> staticAnalysisService.analyzeFile(file));
            staticAnalysisFutures.add(staticAnalysisFuture);
            
            // Generate AI review
            CompletableFuture<AIReview> aiReviewFuture = 
                    CompletableFuture.supplyAsync(() -> aiReviewService.generateReview(file));
            aiReviewFutures.add(aiReviewFuture);
        }
        
        // Wait for all analysis tasks to complete
        CompletableFuture<Void> allAnalyses = CompletableFuture.allOf(
                staticAnalysisFutures.toArray(new CompletableFuture[0]));
        
        CompletableFuture<Void> allReviews = CompletableFuture.allOf(
                aiReviewFutures.toArray(new CompletableFuture[0]));
        
        // When all analyses are complete, combine results and post comments
        allAnalyses.thenCombine(allReviews, (v1, v2) -> {
            List<AnalysisReport> reports = staticAnalysisFutures.stream()
                    .map(CompletableFuture::join)
                    .toList();
            
            List<AIReview> reviews = aiReviewFutures.stream()
                    .map(CompletableFuture::join)
                    .toList();
            
            return createReviewComment(pullRequest, reports, reviews);
        }).thenAccept(comment -> {
            gitHubCommentService.postComment(pullRequest, comment);
        }).exceptionally(ex -> {
            logger.error("Error during pull request analysis: {}", ex.getMessage(), ex);
            return null;
        });
    }
    
    /**
     * Create a review comment by combining static analysis and AI review results
     */
    private ReviewComment createReviewComment(PullRequest pullRequest, 
                                              List<AnalysisReport> reports, 
                                              List<AIReview> reviews) {
        StringBuilder commentBuilder = new StringBuilder();
        
        commentBuilder.append("# CodeCritic-J Analysis\n\n");
        commentBuilder.append("Analysis completed at: ").append(Instant.now()).append("\n\n");
        
        // Add static analysis summary
        commentBuilder.append("## Static Analysis Results\n\n");
        if (reports.isEmpty()) {
            commentBuilder.append("No static analysis results available.\n\n");
        } else {
            int totalIssues = reports.stream()
                    .map(r -> r.getIssues().size())
                    .reduce(0, Integer::sum);
            
            commentBuilder.append("Found ").append(totalIssues).append(" issue(s) across ")
                    .append(reports.size()).append(" file(s).\n\n");
            
            for (AnalysisReport report : reports) {
                if (!report.getIssues().isEmpty()) {
                    commentBuilder.append("### ").append(report.getFileId()).append("\n\n");
                    
                    for (AnalysisIssue issue : report.getIssues()) {
                        commentBuilder.append("- **").append(issue.getSeverity()).append("**: ");
                        commentBuilder.append(issue.getDescription()).append(" (Line ").append(issue.getLineNumber()).append(")\n");
                    }
                    commentBuilder.append("\n");
                }
            }
        }
        
        // Add AI review results
        commentBuilder.append("## AI Code Review\n\n");
        if (reviews.isEmpty()) {
            commentBuilder.append("No AI reviews available.\n\n");
        } else {
            logger.info("Adding {} AI reviews to comment", reviews.size());
            for (AIReview review : reviews) {
                if (review.getAiFeedback() != null && !review.getAiFeedback().isEmpty()) {
                    commentBuilder.append(review.getAiFeedback()).append("\n\n");
                    logger.info("Added AI review feedback: {}", review.getAiFeedback().substring(0, 
                        Math.min(100, review.getAiFeedback().length())) + "...");
                }
            }
        }
        
        String content = commentBuilder.toString();
        logger.info("Created review comment with {} characters", content.length());
        
        return ReviewComment.builder()
                .commentId(UUID.randomUUID().toString())
                .content(content)
                .createdAt(Instant.now())
                .build();
    }
} 
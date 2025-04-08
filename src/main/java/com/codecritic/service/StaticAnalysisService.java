package com.codecritic.service;

import com.codecritic.model.ModifiedFile;
import com.codecritic.model.AnalysisReport;
import com.codecritic.model.AnalysisIssue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleViolation;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Properties;

@Service
public class StaticAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(StaticAnalysisService.class);
    
    @Value("${static.analysis.pmd.enabled:true}")
    private boolean pmdEnabled;
    
    @Value("${static.analysis.checkstyle.enabled:true}")
    private boolean checkstyleEnabled;
    
    /**
     * Analyzes a file using static analysis tools and returns a report
     */
    public AnalysisReport analyzeFile(ModifiedFile file) {
        List<AnalysisIssue> issues = new ArrayList<>();
        
        try {
            // Create a temporary file with the content
            Path tempFilePath = createTempFile(file);
            
            // Run PMD analysis if enabled
            if (pmdEnabled && file.getFilePath().endsWith(".java")) {
                List<AnalysisIssue> pmdIssues = runPMDAnalysis(tempFilePath.toFile());
                issues.addAll(pmdIssues);
            }
            
            // Run Checkstyle analysis if enabled
            if (checkstyleEnabled && file.getFilePath().endsWith(".java")) {
                List<AnalysisIssue> checkstyleIssues = runCheckstyleAnalysis(tempFilePath.toFile());
                issues.addAll(checkstyleIssues);
            }
            
            // Clean up temp file
            Files.deleteIfExists(tempFilePath);
            
        } catch (Exception e) {
            logger.error("Error during static analysis of file {}: {}", 
                    file.getFileName(), e.getMessage(), e);
            
            // Add an error message as an issue
            issues.add(AnalysisIssue.builder()
                    .issueId(UUID.randomUUID().toString())
                    .description("Error analyzing file: " + e.getMessage())
                    .severity(AnalysisIssue.Severity.HIGH)
                    .lineNumber(0)
                    .build());
        }
        
        return AnalysisReport.builder()
                .reportId(UUID.randomUUID().toString())
                .fileId(file.getFileId())
                .generatedAt(Instant.now())
                .issues(issues)
                .build();
    }
    
    /**
     * Creates a temporary file with the diff content
     */
    private Path createTempFile(ModifiedFile file) throws IOException {
        Path tempFilePath = Files.createTempFile("codecritic_", "_" + file.getFileName());
        try (FileWriter writer = new FileWriter(tempFilePath.toFile())) {
            writer.write(file.getDiffContent());
        }
        return tempFilePath;
    }
    
    /**
     * Runs PMD analysis on a file
     */
    private List<AnalysisIssue> runPMDAnalysis(File file) {
        List<AnalysisIssue> issues = new ArrayList<>();
        
        try {
            // Configure PMD
            PMDConfiguration config = new PMDConfiguration();
            config.setInputFilePath(file.getAbsolutePath());
            config.setRuleSets("category/java/bestpractices.xml,category/java/errorprone.xml");
            
            // Use the current non-deprecated API
            PMD.runPmd(config);
            
            // In a real implementation, we would capture violations
            
        } catch (Exception e) {
            logger.error("PMD analysis error: {}", e.getMessage(), e);
        }
        
        return issues;
    }
    
    /**
     * Runs Checkstyle analysis on a file
     */
    private List<AnalysisIssue> runCheckstyleAnalysis(File file) {
        List<AnalysisIssue> issues = new ArrayList<>();
        
        try {
            // Load Checkstyle configuration
            Properties properties = new Properties();
            
            // Create a property resolver for Checkstyle
            com.puppycrawl.tools.checkstyle.PropertyResolver resolver = 
                propertyName -> properties.getProperty(propertyName);
            
            // Use our custom configuration file
            Configuration config = ConfigurationLoader.loadConfiguration(
                    getClass().getClassLoader().getResource("checkstyle.xml").toExternalForm(),
                    resolver);
            
            Checker checker = new Checker();
            checker.setModuleClassLoader(getClass().getClassLoader());
            checker.configure(config);
            
            // Create a listener for violations
            List<AuditEvent> events = new ArrayList<>();
            checker.addListener(new AuditListener() {
                @Override
                public void auditStarted(AuditEvent event) {}
                
                @Override
                public void auditFinished(AuditEvent event) {}
                
                @Override
                public void fileStarted(AuditEvent event) {}
                
                @Override
                public void fileFinished(AuditEvent event) {}
                
                @Override
                public void addError(AuditEvent event) {
                    events.add(event);
                }
                
                @Override
                public void addException(AuditEvent event, Throwable throwable) {
                    logger.error("Checkstyle exception: {}", throwable.getMessage());
                }
            });
            
            // Run Checkstyle
            List<File> files = new ArrayList<>();
            files.add(file);
            checker.process(files);
            
            // Convert Checkstyle violations to analysis issues
            for (AuditEvent event : events) {
                SeverityLevel severityLevel = event.getSeverityLevel();
                AnalysisIssue.Severity severity;
                
                if (severityLevel == SeverityLevel.ERROR) {
                    severity = AnalysisIssue.Severity.MEDIUM;
                } else if (severityLevel == SeverityLevel.WARNING) {
                    severity = AnalysisIssue.Severity.LOW;
                } else {
                    severity = AnalysisIssue.Severity.LOW;
                }
                
                issues.add(AnalysisIssue.builder()
                        .issueId(UUID.randomUUID().toString())
                        .description(event.getMessage())
                        .severity(severity)
                        .lineNumber(event.getLine())
                        .columnNumber(event.getColumn())
                        .build());
            }
            
            checker.destroy();
            
        } catch (CheckstyleException e) {
            logger.error("Checkstyle analysis error: {}", e.getMessage(), e);
        }
        
        return issues;
    }
} 
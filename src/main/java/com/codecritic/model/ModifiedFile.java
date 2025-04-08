package com.codecritic.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ModifiedFile {
    private String fileId;
    private String fileName;
    private String filePath;
    private ChangeType changeType;
    private String diffContent;
    
    public enum ChangeType {
        ADDED,
        MODIFIED,
        DELETED
    }
} 
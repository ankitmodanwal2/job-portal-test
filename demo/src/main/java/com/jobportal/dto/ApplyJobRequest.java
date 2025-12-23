package com.jobportal.dto;

import lombok.Data;

@Data
public class ApplyJobRequest {
    private Long jobId;
    // REMOVED: userId - will be extracted from JWT token
}
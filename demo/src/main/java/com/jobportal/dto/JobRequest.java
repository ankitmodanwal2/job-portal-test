package com.jobportal.dto;

import com.jobportal.enums.JobType;
import lombok.Data;

@Data
public class JobRequest {
    private String title;
    private String description;
    private String location;
    private String companyName;
    private Double salary;
    private JobType jobType;
    // REMOVED: employerId - will be extracted from JWT token
}
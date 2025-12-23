package com.jobportal.controller;

import com.jobportal.dto.ApplicationResponse;
import com.jobportal.dto.ApplyJobRequest;
import com.jobportal.dto.UpdateStatusRequest;
import com.jobportal.response.ApiResponse;
import com.jobportal.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> apply(@RequestBody ApplyJobRequest request) {
        return ResponseEntity.ok(jobApplicationService.apply(request));
    }

    // CONSIDER: Remove userId from path and get from JWT instead
    // Or keep it but add authorization check in service
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(jobApplicationService.getApplicationByUser(userId));
    }

    // Better alternative: Get current user's applications without userId in path
    @GetMapping("/my-applications")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplications() {
        // Service layer will extract user from JWT
        return ResponseEntity.ok(jobApplicationService.getMyApplications());
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobApplicationService.getApplicationByJob(jobId));
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long applicationId,
            @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(jobApplicationService.updateStatus(applicationId, request));
    }
}
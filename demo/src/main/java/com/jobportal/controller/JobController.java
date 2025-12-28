package com.jobportal.controller;

import com.jobportal.dto.JobRequest;
import com.jobportal.dto.JobResponse;
import com.jobportal.response.ApiResponse;
import com.jobportal.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PreAuthorize("hasRole('ROLE_EMPLOYER')")
    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> createJob(@RequestBody JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getAllJobs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "createdAt") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(jobService.getAllJobs(page, size, sortBy, sortDirection));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> search(@RequestParam(required = false) String keyword, @RequestParam(required = false) String location, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size ) {
        return ResponseEntity.ok(jobService.searchJobs(keyword, location, page, size ));
    }

}

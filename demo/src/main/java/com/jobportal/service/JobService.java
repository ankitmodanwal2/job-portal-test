package com.jobportal.service;

import com.jobportal.dto.JobRequest;
import com.jobportal.dto.JobResponse;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public ApiResponse<JobResponse> createJob(JobRequest request) {

        // Get authenticated user from security context
        String email = getAuthenticatedUserEmail();

        User employer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));

        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setCompanyName(request.getCompanyName());
        job.setSalary(request.getSalary());
        job.setJobType(request.getJobType());
        job.setPostedBy(employer);
        job.setCreatedAt(LocalDateTime.now());

        Job savedJob = jobRepository.save(job);

        return ApiResponse.success("Job created successfully", mapToResponse(savedJob));
    }

    public ApiResponse<Page<JobResponse>> getAllJobs(int page, int size, String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<JobResponse> jobs = jobRepository.findAll(pageable)
                .map(this::mapToResponse);

        return ApiResponse.success("Jobs fetched successfully", jobs);
    }

    public ApiResponse<JobResponse> getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return ApiResponse.success("Job fetched successfully", mapToResponse(job));
    }

    public ApiResponse<Page<JobResponse>> searchJobs(String keyword, String location, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobs;

        if(keyword != null && location != null) {
            jobs = jobRepository.findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(keyword, location, pageable);
        }
        else if(keyword != null) {
            jobs = jobRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        }
        else if(location != null) {
            jobs = jobRepository.findByLocationContainingIgnoreCase(location, pageable);
        }
        else {
            jobs = jobRepository.findAll(pageable);
        }
        return ApiResponse.success("Jobs fetched successfully", jobs.map(this::mapToResponse));
    }

    private JobResponse mapToResponse(Job job) {
        JobResponse jobResponse = new JobResponse();
        jobResponse.setId(job.getId());
        jobResponse.setTitle(job.getTitle());
        jobResponse.setDescription(job.getDescription());
        jobResponse.setLocation(job.getLocation());
        jobResponse.setCompanyName(job.getCompanyName());
        jobResponse.setSalary(job.getSalary());
        jobResponse.setJobType(job.getJobType());
        jobResponse.setPostedBy(job.getPostedBy().getName());
        jobResponse.setCreatedAt(job.getCreatedAt());
        return jobResponse;
    }

    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // Returns email since we use email as username
    }
}
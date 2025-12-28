package com.jobportal.service;

import com.jobportal.dto.ApplicationResponse;
import com.jobportal.dto.ApplyJobRequest;
import com.jobportal.dto.UpdateStatusRequest;
import com.jobportal.entity.Job;
import com.jobportal.entity.JobApplication;
import com.jobportal.entity.User;
import com.jobportal.enums.ApplicationStatus;
import com.jobportal.exception.AccessDeniedException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public ApiResponse<Void> apply(ApplyJobRequest request) {

        // Get authenticated user from security context
        String email = getAuthenticatedUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if(jobApplicationRepository.existsByJobIdAndUserId(request.getJobId(), user.getId())) {
            throw new RuntimeException("Already applied for this job");
        }

        JobApplication jobApplication = new JobApplication();
        jobApplication.setJob(job);
        jobApplication.setUser(user);
        jobApplication.setStatus(ApplicationStatus.APPLIED);
        jobApplication.setAppliedAt(LocalDateTime.now());

        jobApplicationRepository.save(jobApplication);
        return ApiResponse.success("Job applied successfully", null);
    }

    public ApiResponse<List<ApplicationResponse>> getApplicationByUser(Long userId) {

        // Optional: Add authorization check
        String email = getAuthenticatedUserEmail();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Only allow users to see their own applications or employers to see all
        if (!currentUser.getId().equals(userId) &&
                !currentUser.getRole().name().equals("EMPLOYER")) {
            throw new RuntimeException("Unauthorized to view these applications");
        }

        List<ApplicationResponse> responses = jobApplicationRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success("List of applications fetched successfully", responses);
    }

    public ApiResponse<List<ApplicationResponse>> getApplicationByJob(Long jobId) {

        // Optional: Add authorization check - only job poster should see applications
        String email = getAuthenticatedUserEmail();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to view applications for this job");
        }

        List<ApplicationResponse> responses = jobApplicationRepository.findByJobId(jobId)
                .stream()
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success("List of applications fetched successfully", responses);
    }

    public ApiResponse<Void> updateStatus(Long applicationId, UpdateStatusRequest request) {

        // Optional: Add authorization check - only job poster can update status
        String email = getAuthenticatedUserEmail();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getJob().getPostedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to update this application");
        }

        application.setStatus(request.getStatus());
        jobApplicationRepository.save(application);
        return ApiResponse.success("Application status updated successfully", null);
    }

    private ApplicationResponse mapToResponse(JobApplication jobApplication) {
        ApplicationResponse applicationResponse = new ApplicationResponse();
        applicationResponse.setApplicationId(jobApplication.getId());
        applicationResponse.setJobTitle(jobApplication.getJob().getTitle());
        applicationResponse.setCompanyName(jobApplication.getJob().getCompanyName());
        applicationResponse.setApplicantName(jobApplication.getUser().getName());
        applicationResponse.setStatus(jobApplication.getStatus());
        applicationResponse.setAppliedAt(jobApplication.getAppliedAt());
        return applicationResponse;
    }
    public ApiResponse<List<ApplicationResponse>> getMyApplications() {
        String email = getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<ApplicationResponse> responses = jobApplicationRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success("List of applications fetched successfully", responses);
    }

    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
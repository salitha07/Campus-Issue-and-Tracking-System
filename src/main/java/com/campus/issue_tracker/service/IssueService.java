package com.campus.issue_tracker.service;

import com.campus.issue_tracker.dto.IssueRequest;
import com.campus.issue_tracker.entity.*;
import com.campus.issue_tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    public Issue createIssue(IssueRequest request, String username) {
        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setLocation(request.getLocation());
        issue.setReporter(reporter);
        issue.setStatus(IssueStatus.PENDING);
        issue.setLatitude(request.getLatitude());
        issue.setLongitude(request.getLongitude());

        return issueRepository.save(issue);
    }

    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    public Issue updateStatus(Long issueId, IssueStatus status) {
        if (issueId == null)
            throw new IllegalArgumentException("Issue ID cannot be null");
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        issue.setStatus(status);
        Issue updatedIssue = issueRepository.save(issue);

        // Send email to the reporter
        try {
            emailService.sendStatusUpdateEmail(
                    updatedIssue.getReporter().getEmail(),
                    updatedIssue.getId(),
                    status.name());
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
        }

        return updatedIssue;
    }

    public Issue addStudentFeedback(Long issueId, String feedback, Integer rating, String username) {
        if (issueId == null)
            throw new IllegalArgumentException("Issue ID cannot be null");
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        if (!issue.getReporter().getUsername().equals(username)) {
            throw new RuntimeException("Only the reporter can provide feedback");
        }

        if (issue.getStatus() == IssueStatus.PENDING) {
            throw new RuntimeException("Feedback can only be provided after staff action");
        }

        if ((feedback == null || feedback.trim().isEmpty()) && (rating == null || rating == 0)) {
            throw new RuntimeException("At least a rating or feedback text is required");
        }

        issue.setStudentFeedback(feedback);
        issue.setRating(rating);
        return issueRepository.save(issue);
    }

    public Page<Issue> getIssuesPaged(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return issueRepository.findAll(pageable);
    }

    public Issue saveDirectly(@org.springframework.lang.NonNull Issue issue) {
        return issueRepository.save(issue);
    }

    public boolean isPotentialDuplicate(String title, String location) {
        List<IssueStatus> activeStatuses = List.of(IssueStatus.PENDING, IssueStatus.IN_PROGRESS);
        List<Issue> duplicates = issueRepository.findByTitleAndLocationAndStatusIn(title, location, activeStatuses);
        return !duplicates.isEmpty();
    }
}

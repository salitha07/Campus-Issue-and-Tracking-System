package com.campus.issue_tracker.service;

import com.campus.issue_tracker.dto.IssueRequest;
import com.campus.issue_tracker.entity.*;
import com.campus.issue_tracker.repository.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        if (request.getLocation() != null && !request.getLocation().isEmpty()) {
            issue.setLocation(request.getLocation());
        } else {
            // Auto-generate location based on category
            if (request.getCategory() == IssueCategory.ACADEMIC) {
                issue.setLocation("Academic Issue: " + request.getCourseUnit());
            } else if (request.getCategory() == IssueCategory.HOSTEL) {
                issue.setLocation("Hostel: " + request.getHostelBlock() + ", Room: " + request.getRoomNumber());
            } else if (request.getCategory() == IssueCategory.PAYMENTS) {
                issue.setLocation("Payment Issue: " + request.getPaymentId());
            } else {
                issue.setLocation("Not Specified");
            }
        }

        issue.setLatitude(request.getLatitude());
        issue.setLongitude(request.getLongitude());
        issue.setReporter(reporter);
        issue.setStatus(IssueStatus.PENDING);

        // anonymous
        issue.setAnonymous(request.isAnonymous());
        issue.setCategory(request.getCategory());

        // Category specific fields
        issue.setCourseUnit(request.getCourseUnit());
        issue.setPaymentId(request.getPaymentId());
        issue.setHostelBlock(request.getHostelBlock());
        issue.setRoomNumber(request.getRoomNumber());

        // ✅ escalation initial setup
        issue.setEscalated(false);
        issue.setEscalationLevel(0);
        issue.setEscalationTime(LocalDateTime.now());

        Issue savedIssue = issueRepository.save(issue);

        // Send Email
        try {
            emailService.sendIssueCreatedEmail(savedIssue.getReporter().getEmail(), savedIssue);
        } catch (Exception e) {
            System.err.println("Error sending creation email: " + e.getMessage());
        }

        return savedIssue;
    }

    public Issue saveDirectly(Issue issue) {
        return issueRepository.save(issue);
    }

    public List<Issue> getAllIssues() {
        return issueRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Issue updateStatus(Long id, IssueStatus newStatus) {

        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        IssueStatus oldStatus = issue.getStatus();
        issue.setStatus(newStatus);

        // ✅ reset escalation when status manually changed
        issue.setEscalated(false);
        issue.setEscalationLevel(0);
        issue.setEscalationTime(LocalDateTime.now());

        Issue savedIssue = issueRepository.save(issue);

        // Send Email
        try {
            emailService.sendIssueStatusUpdatedEmail(savedIssue.getReporter().getEmail(), savedIssue, oldStatus);
        } catch (Exception e) {
            System.err.println("Error sending status update email: " + e.getMessage());
        }

        return savedIssue;
    }

    public Issue addStudentFeedback(Long id, String feedback, Integer rating, String username) {

        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        if (!issue.getReporter().getUsername().equals(username)) {
            throw new RuntimeException("You can only provide feedback for your own issue");
        }

        issue.setStudentFeedback(feedback);
        issue.setRating(rating);

        return issueRepository.save(issue);
    }

    public Page<Issue> getIssuesPaged(int page, int size, String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return issueRepository.findAll(pageable);
    }

    public Page<Issue> getIssuesWithFilters(int page, int size, String sortBy, String direction,
            String query, IssueCategory category, IssueStatus status, String dateRange) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Issue> spec = IssueSpecification.filterIssues(query, category, status, dateRange);

        return issueRepository.findAll(spec, pageable);
    }

    public boolean isPotentialDuplicate(String title, String location) {
        return issueRepository.existsByTitleAndLocation(title, location);
    }
}

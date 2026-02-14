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

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

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

        // Audit Log
        auditLogService.logEvent(
                "ISSUE_CREATED",
                savedIssue.getId(),
                "Issue",
                username,
                "Created issue: " + savedIssue.getTitle());

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

        // Audit Log
        // We don't have the current user here directly, but we can assume it's a
        // staff/admin action.
        // In a real app, we'd pass the principal. For now, we'll log "System/Staff".
        // OR we can update the controller to pass the username.
        // Let's rely on the controller passing the username or use
        // SecurityContextHolder in Service (cleaner but harder to mock).
        // For simplicity, let's update ONLY the method signature or use
        // SecurityContextHolder if needed.
        // Actually, the simplest way without changing method sigs too much is to use
        // SecurityContextHolder or just log "Staff".
        // But wait, the controller has Authentication! I should update the controller
        // to pass username.
        // However, to avoid changing the controller right now and minimize diffs, I'll
        // use SecurityContextHolder
        // OR just log it. Let's start with a generic "Staff/Admin" or check if I can
        // easily update controller.
        // Looking at IssueController.java... updateStatus takes Authentication? No.
        // Let's JUST log it without username for now, or update controller.
        // Actually, I'll update the controller too, that's better. But wait, I can't
        // multi-file edit easily.
        // Let's use SecurityContextHolder to get the current user!

        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();

        auditLogService.logEvent(
                "STATUS_UPDATED",
                savedIssue.getId(),
                "Issue",
                username,
                "Changed status from " + oldStatus + " to " + newStatus);

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

        Issue savedIssue = issueRepository.save(issue);

        // Audit Log
        auditLogService.logEvent(
                "FEEDBACK_ADDED",
                savedIssue.getId(),
                "Issue",
                username,
                "Added feedback. Rating: " + rating);

        return savedIssue;
    }

    public Page<Issue> getIssuesPaged(int page, int size, String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return issueRepository.findAll(pageable);
    }

    public boolean isPotentialDuplicate(String title, IssueCategory category, String location) {
        return issueRepository.existsByTitleIgnoreCaseAndCategoryAndLocationIgnoreCase(title, category, location);
    }
}

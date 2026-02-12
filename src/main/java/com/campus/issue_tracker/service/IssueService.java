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

    public Issue createIssue(IssueRequest request, String username) {

        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setLocation(request.getLocation());
        issue.setLatitude(request.getLatitude());
        issue.setLongitude(request.getLongitude());
        issue.setReporter(reporter);
        issue.setStatus(IssueStatus.PENDING);

        // anonymous
        issue.setAnonymous(request.isAnonymous());

        // ✅ escalation initial setup
        issue.setEscalated(false);
        issue.setEscalationLevel(0);
        issue.setEscalationTime(LocalDateTime.now());

        return issueRepository.save(issue);
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

        issue.setStatus(newStatus);

        // ✅ reset escalation when status manually changed
        issue.setEscalated(false);
        issue.setEscalationLevel(0);
        issue.setEscalationTime(LocalDateTime.now());

        return issueRepository.save(issue);
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

    public boolean isPotentialDuplicate(String title, String location) {
        return issueRepository.existsByTitleAndLocation(title, location);
    }
}

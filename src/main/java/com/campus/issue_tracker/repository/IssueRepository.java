package com.campus.issue_tracker.repository;

import com.campus.issue_tracker.entity.Issue;
import com.campus.issue_tracker.entity.IssueCategory;
import com.campus.issue_tracker.entity.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

        // ✅ duplicate checking by Title + Category + Location
        boolean existsByTitleIgnoreCaseAndCategoryAndLocationIgnoreCase(String title, IssueCategory category,
                        String location);

        // ✅ find issues by status
        List<Issue> findByStatus(IssueStatus status);

        // ✅ get all escalated issues
        List<Issue> findByEscalatedTrue();

        // ✅ get high escalation level issues
        List<Issue> findByEscalationLevelGreaterThan(int level);

        // 🔥 PRODUCTION OPTIMIZED ESCALATION QUERIES

        // Level 1 → PENDING older than 6 hours
        List<Issue> findByStatusAndCreatedAtBefore(
                        IssueStatus status,
                        LocalDateTime time);

        // Level 2 → IN_PROGRESS older than 12 hours
        List<Issue> findByStatusAndUpdatedAtBefore(
                        IssueStatus status,
                        LocalDateTime time);
}

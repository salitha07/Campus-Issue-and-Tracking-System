package com.campus.issue_tracker.repository;

import com.campus.issue_tracker.entity.Issue;
import com.campus.issue_tracker.entity.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    // This method returns a "Page" of issues instead of a "List"
    Page<Issue> findByStatus(IssueStatus status, Pageable pageable);

    List<Issue> findByTitleAndLocationAndStatusIn(String title, String location, List<IssueStatus> statuses);
}
package com.campus.issue_tracker.repository;

import com.campus.issue_tracker.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    // ✅ add this for duplicate checking
    boolean existsByTitleAndLocation(String title, String location);
}

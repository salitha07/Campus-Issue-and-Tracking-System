package com.campus.issue_tracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000) // Allows for a long description
    private String description;

    @Column(nullable = false)
    private String location; // e.g., "Library Room 2"
    // Map coordinates (OpenStreetMap / Google Maps)
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private IssueStatus status = IssueStatus.PENDING;

    // The Relationship: Many issues belong to one reporter (User)
    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String attachmentPath;

    @Column(length = 1000)
    private String studentFeedback;

    private Integer rating;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
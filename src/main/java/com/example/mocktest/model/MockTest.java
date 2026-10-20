package com.example.mocktest.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mock_tests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private Integer durationMinutes;

    private Integer totalMarks;

    private Integer passingMarks;

    private boolean freeAccess;

    private boolean active = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "mockTest",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TestQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "mockTest")
    private List<TestAttempt> attempts = new ArrayList<>();
}

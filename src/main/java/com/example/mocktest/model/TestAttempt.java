package com.example.mocktest.model;

import com.example.mocktest.model.enums.AttemptStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mock_test_id", nullable = false)
    private MockTest mockTest;

    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;

    private Integer score;

    private Integer totalMarks;

    private Integer correctAnswers;

    private Integer incorrectAnswers;

    private Integer unanswered;

    @Enumerated(EnumType.STRING)
    private AttemptStatus status;

    @OneToMany(
            mappedBy = "attempt",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<UserAnswer> answers = new ArrayList<>();
}

package com.example.mocktest.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "test_questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"mock_test_id", "question_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mock_test_id", nullable = false)
    private MockTest mockTest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private Integer questionOrder;

    private Integer marks;
}

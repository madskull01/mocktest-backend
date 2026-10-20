package com.example.mocktest.controller;

import com.example.mocktest.dto.question.AdminQuestionResponse;
import com.example.mocktest.dto.question.CreateQuestionRequest;
import com.example.mocktest.dto.question.UpdateQuestionRequest;
import com.example.mocktest.model.enums.Difficulty;
import com.example.mocktest.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping(
            value = "/subjects/{subjectId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminQuestionResponse> createQuestion(
            @PathVariable Long subjectId,
            @Valid @RequestBody CreateQuestionRequest request) {

        AdminQuestionResponse response =
                questionService.createQuestion(
                        subjectId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping(
            value = "/subjects/{subjectId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminQuestionResponse> createQuestionWithImage(
            @PathVariable Long subjectId,
            @Valid @RequestPart("question") CreateQuestionRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        AdminQuestionResponse response =
                questionService.createQuestion(subjectId, request, image);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<AdminQuestionResponse>>
    getAllQuestions() {

        return ResponseEntity.ok(
                questionService.getAllQuestions()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminQuestionResponse>
    getQuestionById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                questionService.getQuestionById(id)
        );
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<AdminQuestionResponse>>
    getQuestionsBySubject(
            @PathVariable Long subjectId) {

        return ResponseEntity.ok(
                questionService.getQuestionsBySubject(
                        subjectId
                )
        );
    }

    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<AdminQuestionResponse>>
    getQuestionsByDifficulty(
            @PathVariable Difficulty difficulty) {

        return ResponseEntity.ok(
                questionService.getQuestionsByDifficulty(
                        difficulty
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminQuestionResponse>
    updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuestionRequest request) {

        return ResponseEntity.ok(
                questionService.updateQuestion(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id) {

        questionService.deleteQuestion(id);

        return ResponseEntity.noContent().build();
    }
}

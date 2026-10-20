package com.example.mocktest.service;


import com.example.mocktest.dto.question.AdminQuestionResponse;
import com.example.mocktest.dto.question.CreateQuestionRequest;
import com.example.mocktest.dto.question.UpdateQuestionRequest;
import com.example.mocktest.model.Question;
import com.example.mocktest.model.enums.Difficulty;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QuestionService {

    AdminQuestionResponse createQuestion(
            Long subjectId,
            CreateQuestionRequest request
    );

    AdminQuestionResponse createQuestion(
            Long subjectId,
            CreateQuestionRequest request,
            MultipartFile image
    );

    AdminQuestionResponse getQuestionById(Long id);

    List<AdminQuestionResponse> getAllQuestions();

    List<AdminQuestionResponse> getQuestionsBySubject(
            Long subjectId
    );

    List<AdminQuestionResponse> getQuestionsByDifficulty(
            Difficulty difficulty
    );

    AdminQuestionResponse updateQuestion(
            Long id,
            UpdateQuestionRequest request
    );

    void deleteQuestion(Long id);
}
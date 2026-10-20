package com.example.mocktest.service;


import com.example.mocktest.model.QuestionOption;

import java.util.List;

public interface QuestionOptionService {

    QuestionOption createOption(
            Long questionId,
            QuestionOption option);

    List<QuestionOption> getOptionsByQuestion(Long questionId);

    QuestionOption getOptionById(Long id);

    QuestionOption updateOption(
            Long id,
            QuestionOption option);

    void deleteOption(Long id);
}

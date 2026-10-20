package com.example.mocktest.service.impl;

import com.example.mocktest.model.Question;
import com.example.mocktest.model.QuestionOption;
import com.example.mocktest.repository.QuestionOptionRepository;
import com.example.mocktest.repository.QuestionRepository;
import com.example.mocktest.service.QuestionOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionOptionServiceImpl
        implements QuestionOptionService {

    private final QuestionOptionRepository optionRepository;
    private final QuestionRepository questionRepository;

    @Override
    public QuestionOption createOption(
            Long questionId,
            QuestionOption option) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() ->
                        new RuntimeException("Question not found"));

        long count = optionRepository.countByQuestionId(questionId);

        if (count >= 4) {
            throw new RuntimeException(
                    "A question cannot have more than 4 options");
        }

        if (option.isCorrect()) {

            long correctCount =
                    optionRepository
                            .countByQuestionIdAndCorrectTrue(questionId);

            if (correctCount >= 1) {
                throw new RuntimeException(
                        "Question already has a correct option");
            }
        }

        option.setQuestion(question);

        return optionRepository.save(option);
    }

    @Override
    public List<QuestionOption> getOptionsByQuestion(
            Long questionId) {

        return optionRepository.findByQuestionId(questionId);
    }

    @Override
    public QuestionOption getOptionById(Long id) {

        return optionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Option not found"));
    }

    @Override
    public QuestionOption updateOption(
            Long id,
            QuestionOption option) {

        QuestionOption existing = getOptionById(id);

        existing.setOptionText(option.getOptionText());

        return optionRepository.save(existing);
    }

    @Override
    public void deleteOption(Long id) {

        QuestionOption option = getOptionById(id);

        optionRepository.delete(option);
    }
}
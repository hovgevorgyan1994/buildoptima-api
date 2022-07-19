package com.vecondev.buildoptima.service.faq;

import com.vecondev.buildoptima.dto.request.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.faq.FaqQuestionRequestDto;
import com.vecondev.buildoptima.dto.response.FetchResponseDto;
import com.vecondev.buildoptima.dto.response.faq.FaqQuestionResponseDto;
import com.vecondev.buildoptima.model.faq.FaqQuestion;

import java.util.List;
import java.util.UUID;

public interface FaqQuestionService {

  List<FaqQuestionResponseDto> getAllQuestions();

  FaqQuestionResponseDto getQuestionById(UUID questionId);

  FaqQuestionResponseDto createQuestion(FaqQuestionRequestDto requestDto, UUID userId);

  FaqQuestionResponseDto updateQuestion(
      UUID questionId, FaqQuestionRequestDto requestDto, UUID userId);

  void deleteQuestion(UUID questionId, UUID userId);

  FaqQuestion findQuestionById(UUID questionId);

  FetchResponseDto fetchQuestions(FetchRequestDto fetchRequest);
}

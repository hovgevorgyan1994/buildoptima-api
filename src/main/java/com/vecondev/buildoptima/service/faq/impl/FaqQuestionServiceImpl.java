package com.vecondev.buildoptima.service.faq.impl;

import com.vecondev.buildoptima.dto.request.faq.FaqQuestionRequestDto;
import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.response.faq.FaqQuestionResponseDto;
import com.vecondev.buildoptima.dto.response.filter.FetchResponseDto;
import com.vecondev.buildoptima.exception.FaqQuestionNotFoundException;
import com.vecondev.buildoptima.filter.converter.PageableConverter;
import com.vecondev.buildoptima.filter.model.SortDto;
import com.vecondev.buildoptima.filter.specification.GenericSpecification;
import com.vecondev.buildoptima.mapper.faq.FaqQuestionMapper;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import com.vecondev.buildoptima.model.faq.FaqQuestion;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.faq.FaqQuestionRepository;
import com.vecondev.buildoptima.service.faq.FaqCategoryService;
import com.vecondev.buildoptima.service.faq.FaqQuestionService;
import com.vecondev.buildoptima.service.user.UserService;
import com.vecondev.buildoptima.validation.faq.FaqQuestionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.vecondev.buildoptima.exception.ErrorCode.FAQ_QUESTION_NOT_FOUND;
import static com.vecondev.buildoptima.filter.model.FaqQuestionFields.faqQuestionPageSortingFieldsMap;
import static com.vecondev.buildoptima.validation.validator.FieldNameValidator.validateFieldNames;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FaqQuestionServiceImpl implements FaqQuestionService {

  private final FaqQuestionMapper faqQuestionMapper;
  private final FaqQuestionValidator faqQuestionValidator;
  private final FaqQuestionRepository faqQuestionRepository;

  private final FaqCategoryService faqCategoryService;
  private final UserService userService;
  private final PageableConverter pageableConverter;

  @Override
  public List<FaqQuestionResponseDto> getAllQuestions() {
    List<FaqQuestion> faqQuestions = faqQuestionRepository.findAll();

    return faqQuestionMapper.mapToListDto(faqQuestions);
  }

  @Override
  public FaqQuestionResponseDto getQuestionById(UUID questionId) {
    return faqQuestionMapper.mapToDto(findQuestionById(questionId));
  }

  @Override
  public FaqQuestionResponseDto createQuestion(FaqQuestionRequestDto requestDto, UUID userId) {
    User user = userService.getUserById(userId);
    FaqCategory faqCategory = faqCategoryService.findCategoryById(requestDto.getFaqCategoryId());
    FaqQuestion faqQuestion = faqQuestionMapper.mapToEntity(requestDto, faqCategory, user);
    faqQuestionValidator.validateQuestion(faqQuestion.getQuestion());

    faqQuestion = faqQuestionRepository.saveAndFlush(faqQuestion);
    log.info("User with id: {} created new FAQ QUESTION with id: {}", userId, faqQuestion.getId());

    return faqQuestionMapper.mapToDto(faqQuestion);
  }

  @Override
  public FaqQuestionResponseDto updateQuestion(
      UUID questionId, FaqQuestionRequestDto requestDto, UUID userId) {
    FaqQuestion question =
        faqQuestionRepository
            .findById(questionId)
            .orElseThrow(() -> new FaqQuestionNotFoundException(FAQ_QUESTION_NOT_FOUND));
    faqQuestionValidator.validateQuestion(requestDto.getQuestion());

    question =
        question.toBuilder()
            .question(requestDto.getQuestion())
            .answer(requestDto.getAnswer())
            .status(requestDto.getStatus())
            .category(faqCategoryService.findCategoryById(requestDto.getFaqCategoryId()))
            .updatedBy(userService.getUserById(userId))
            .build();
    log.info("User with id: {} updated the FAQ Question with id: {}", userId, questionId);

    return faqQuestionMapper.mapToDto(faqQuestionRepository.save(question));
  }

  @Override
  public void deleteQuestion(UUID questionId, UUID userId) {
    if (!faqQuestionRepository.existsById(questionId)) {
      throw new FaqQuestionNotFoundException(FAQ_QUESTION_NOT_FOUND);
    }

    faqQuestionRepository.deleteById(questionId);
    log.info("User with id: {} deleted the FAQ Question with id: {}", userId, questionId);
  }

  @Override
  public FaqQuestion findQuestionById(UUID questionId) {
    return faqQuestionRepository
        .findById(questionId)
        .orElseThrow(() -> new FaqQuestionNotFoundException(FAQ_QUESTION_NOT_FOUND));
  }

  @Override
  public FetchResponseDto fetchQuestions(FetchRequestDto fetchRequest) {
    log.info("Request to fetch FAQ questions from DB");
    validateFieldNames(faqQuestionPageSortingFieldsMap, fetchRequest.getSort());
    if (fetchRequest.getSort() == null || fetchRequest.getSort().isEmpty()) {
      SortDto sortDto = new SortDto("questions", SortDto.Direction.ASC);
      fetchRequest.setSort(List.of(sortDto));
    }
    Pageable pageable = pageableConverter.convert(fetchRequest);
    Specification<FaqQuestion> specification =
        new GenericSpecification<>(faqQuestionPageSortingFieldsMap, fetchRequest.getFilter());
    Page<FaqQuestion> result = faqQuestionRepository.findAll(specification, pageable);

    List<FaqQuestionResponseDto> content = faqQuestionMapper.mapToListDtoFromPage(result);
    log.info("Response was sent. {} results where found", content.size());
    return FetchResponseDto.builder()
        .content(content)
        .page(result.getNumber())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .last(result.isLast())
        .build();
  }
}

package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.dto.request.faq.FaqQuestionRequestDto;
import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.response.faq.FaqQuestionResponseDto;
import com.vecondev.buildoptima.dto.response.filter.FetchResponseDto;
import com.vecondev.buildoptima.exception.FaqQuestionNotFoundException;
import com.vecondev.buildoptima.exception.ResourceNotFoundException;
import com.vecondev.buildoptima.filter.converter.PageableConverter;
import com.vecondev.buildoptima.mapper.faq.FaqQuestionMapper;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import com.vecondev.buildoptima.model.faq.FaqQuestion;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.faq.question.FaqQuestionServiceTestParameters;
import com.vecondev.buildoptima.repository.faq.FaqQuestionRepository;
import com.vecondev.buildoptima.service.faq.FaqCategoryService;
import com.vecondev.buildoptima.service.faq.impl.FaqQuestionServiceImpl;
import com.vecondev.buildoptima.service.user.UserService;
import com.vecondev.buildoptima.validation.faq.FaqQuestionValidator;
import com.vecondev.buildoptima.validation.validator.FieldNameValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FaqQuestionServiceTest {

  private final FaqQuestionServiceTestParameters testParameters =
      new FaqQuestionServiceTestParameters();

  @InjectMocks @Spy private FaqQuestionServiceImpl faqQuestionService;

  @Mock private FaqQuestionMapper faqQuestionMapper;
  @Mock private FaqQuestionValidator faqQuestionValidator;
  @Mock private FaqQuestionRepository faqQuestionRepository;
  @Mock private FaqCategoryService faqCategoryService;
  @Mock private UserService userService;
  @Mock private PageableConverter pageableConverter;

  @Test
  void successfulRetrievalOfAllQuestions() {
    faqQuestionService.getAllQuestions();

    verify(faqQuestionRepository).findAll();
    verify(faqQuestionMapper).mapToListDto(any());
  }

  @Test
  void successfulRetrievalOfQuestionById() {
    UUID questionId = UUID.randomUUID();

    doReturn(null).when(faqQuestionService).findQuestionById(questionId);
    faqQuestionService.getQuestionById(questionId);

    verify(faqQuestionMapper).mapToDto(any());
  }

  @Test
  void failedRetrievalOfQuestionByIdAsNotFound() {
    UUID id = UUID.randomUUID();

    doThrow(ResourceNotFoundException.class).when(faqQuestionService).findQuestionById(id);

    assertThrows(ResourceNotFoundException.class, () -> faqQuestionService.getQuestionById(id));
  }

  @Test
  void successfulQuestionCreation() {
    UUID userId = UUID.randomUUID();
    User user = testParameters.getUserById(userId);
    FaqQuestionRequestDto faqQuestionRequestDto = testParameters.getFaqQuestionRequestDto();
    FaqQuestion faqQuestion = testParameters.getFaqQuestion(userId);
    FaqCategory faqCategory = testParameters.getFaqCategory(userId);
    faqCategory.setId(faqQuestionRequestDto.getFaqCategoryId());
    FaqQuestionResponseDto faqQuestionResponseDto =
        testParameters.getFaqQuestionResponseDto(faqQuestion);

    when(userService.getUserById(userId)).thenReturn(user);
    when(faqCategoryService.findCategoryById(faqQuestionRequestDto.getFaqCategoryId()))
        .thenReturn(faqCategory);
    when(faqQuestionMapper.mapToEntity(faqQuestionRequestDto, faqCategory, user))
        .thenReturn(faqQuestion);
    faqQuestion.setId(UUID.randomUUID());
    when(faqQuestionRepository.saveAndFlush(faqQuestion)).thenReturn(faqQuestion);
    when(faqQuestionMapper.mapToDto(faqQuestion)).thenReturn(faqQuestionResponseDto);

    FaqQuestionResponseDto methodResponse =
        faqQuestionService.createQuestion(faqQuestionRequestDto, userId);
    assertEquals(faqCategory.getId(), methodResponse.getCategory().getId());
    assertEquals(user.getFirstName(), methodResponse.getUpdatedBy().getFirstName());
    verify(faqQuestionValidator).validateQuestion(faqQuestion.getQuestion());
    verify(faqQuestionMapper).mapToDto(faqQuestion);
  }

  @Test
  void failedQuestionCreationAsQuestionIsInvalid() {
    UUID userId = UUID.randomUUID();
    User user = testParameters.getUserById(userId);
    FaqQuestionRequestDto faqQuestionRequestDto = testParameters.getFaqQuestionRequestDto();
    FaqQuestion faqQuestion = testParameters.getFaqQuestion(userId);
    FaqCategory faqCategory = testParameters.getFaqCategory(userId);
    faqCategory.setId(faqQuestionRequestDto.getFaqCategoryId());

    when(userService.getUserById(userId)).thenReturn(user);
    when(faqCategoryService.findCategoryById(faqQuestionRequestDto.getFaqCategoryId()))
        .thenReturn(faqCategory);
    when(faqQuestionMapper.mapToEntity(faqQuestionRequestDto, faqCategory, user))
        .thenReturn(faqQuestion);
    doThrow(IllegalArgumentException.class)
        .when(faqQuestionValidator)
        .validateQuestion(faqQuestion.getQuestion());

    assertThrows(
        IllegalArgumentException.class,
        () -> faqQuestionService.createQuestion(faqQuestionRequestDto, userId));
  }

  @Test
  void successfulQuestionUpdate() {
    String newQuestion = "Question2";
    String newAnswer = "Answer2";
    UUID userId = UUID.randomUUID();
    FaqQuestionRequestDto faqQuestionRequestDto = testParameters.getFaqQuestionRequestDto();
    faqQuestionRequestDto.toBuilder().question(newQuestion).answer(newAnswer).build();
    FaqQuestion faqQuestion = testParameters.getFaqQuestion(userId);
    FaqQuestionResponseDto faqQuestionResponseDto =
        testParameters.getFaqQuestionResponseDto(faqQuestion);
    faqQuestionResponseDto.setQuestion(newQuestion);
    faqQuestionResponseDto.setAnswer(newAnswer);

    when(faqQuestionRepository.findById(faqQuestion.getId())).thenReturn(Optional.of(faqQuestion));
    when(faqQuestionMapper.mapToDto(any())).thenReturn(faqQuestionResponseDto);

    FaqQuestionResponseDto methodResponse =
        faqQuestionService.updateQuestion(faqQuestion.getId(), faqQuestionRequestDto, userId);
    assertEquals(newQuestion, methodResponse.getQuestion());
    assertEquals(faqQuestion.getCategory().getId(), methodResponse.getCategory().getId());
    verify(faqQuestionValidator).validateQuestion(faqQuestionRequestDto.getQuestion());
    verify(faqQuestionMapper).mapToDto(any());
  }

  @Test
  void failedQuestionUpdateAsQuestionNotFound() {
    String newQuestion = "Question2";
    String newAnswer = "Answer2";
    UUID userId = UUID.randomUUID();
    FaqQuestionRequestDto faqQuestionRequestDto = testParameters.getFaqQuestionRequestDto();
    faqQuestionRequestDto.toBuilder().question(newQuestion).answer(newAnswer).build();
    UUID questionId = testParameters.getFaqQuestion(userId).getId();

    when(faqQuestionRepository.findById(questionId)).thenReturn(Optional.empty());

    assertThrows(
        FaqQuestionNotFoundException.class,
        () -> faqQuestionService.updateQuestion(questionId, faqQuestionRequestDto, userId));
  }

  @Test
  void successfulQuestionDeletion() {
    UUID questionId = UUID.randomUUID();

    when(faqQuestionRepository.existsById(questionId)).thenReturn(true);

    faqQuestionService.deleteQuestion(questionId, UUID.randomUUID());
    verify(faqQuestionRepository).deleteById(questionId);
  }

  @Test
  void failedQuestionDeletionAsQuestionNotFound() {
    UUID questionId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    when(faqQuestionRepository.existsById(questionId)).thenReturn(false);

    assertThrows(
        FaqQuestionNotFoundException.class,
        () -> faqQuestionService.deleteQuestion(questionId, userId));
  }

  @Test
  void successfulFindingQuestionById() {
    UUID questionId = UUID.randomUUID();

    when(faqQuestionRepository.findById(questionId)).thenReturn(Optional.of(new FaqQuestion()));

    faqQuestionService.findQuestionById(questionId);
    verify(faqQuestionRepository).findById(questionId);
  }

  @Test
  void failedFindingQuestionByIdAsQuestionNotFound() {
    UUID questionId = UUID.randomUUID();

    when(faqQuestionRepository.findById(questionId)).thenReturn(Optional.empty());

    assertThrows(
        FaqQuestionNotFoundException.class, () -> faqQuestionService.findQuestionById(questionId));
  }

  @Test
  void successfulFetchingOfFaqCategories() {
    FetchRequestDto requestDto = testParameters.getFetchRequest();
    Pageable pageable = testParameters.getPageable(requestDto);
    Page<FaqQuestion> result = new PageImpl<>(testParameters.getFetchResponse());

    try (MockedStatic<FieldNameValidator> validator =
                 Mockito.mockStatic(FieldNameValidator.class)) {
      validator
              .when(() -> FieldNameValidator.validateFieldNames(any(), any()))
              .thenAnswer((Answer<Void>) invocation -> null);
    }
    when(pageableConverter.convert(requestDto)).thenReturn(pageable);
    when(faqQuestionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(result);
    when(faqQuestionMapper.mapToListDtoFromPage(result))
            .thenReturn(testParameters.getFaqQuestionResponseDtoList(result.stream().toList()));

    FetchResponseDto responseDto = faqQuestionService.fetchQuestions(requestDto);
    assertEquals(result.getTotalElements(), responseDto.getTotalElements());
  }

  @Test
  void successfulFetchingOfFaqCategoriesWithDefaultSortDirectory() {
    FetchRequestDto requestDto = testParameters.getFetchRequest();
    Pageable pageable = testParameters.getPageable(requestDto);
    Page<FaqQuestion> result = new PageImpl<>(testParameters.getFetchResponse());

    try (MockedStatic<FieldNameValidator> validator =
                 Mockito.mockStatic(FieldNameValidator.class)) {
      validator
              .when(() -> FieldNameValidator.validateFieldNames(any(), any()))
              .thenAnswer((Answer<Void>) invocation -> null);
    }
    when(pageableConverter.convert(requestDto)).thenReturn(pageable);
    when(faqQuestionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(result);
    when(faqQuestionMapper.mapToListDtoFromPage(result))
            .thenReturn(testParameters.getFaqQuestionResponseDtoList(result.stream().toList()));

    FetchResponseDto responseDto = faqQuestionService.fetchQuestions(requestDto);
    assertEquals(result.getTotalElements(), responseDto.getTotalElements());
  }

}
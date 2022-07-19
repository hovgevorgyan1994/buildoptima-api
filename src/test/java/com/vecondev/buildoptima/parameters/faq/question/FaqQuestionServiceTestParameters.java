package com.vecondev.buildoptima.parameters.faq.question;

import com.vecondev.buildoptima.dto.request.faq.FaqQuestionRequestDto;
import com.vecondev.buildoptima.dto.response.user.UserOverview;
import com.vecondev.buildoptima.dto.response.faq.FaqCategoryOverview;
import com.vecondev.buildoptima.dto.response.faq.FaqQuestionResponseDto;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import com.vecondev.buildoptima.model.faq.FaqQuestion;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.PageableTest;
import com.vecondev.buildoptima.parameters.faq.category.FaqCategoryServiceTestParameters;
import com.vecondev.buildoptima.parameters.user.UserServiceTestParameters;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.vecondev.buildoptima.model.Status.ACTIVE;

public class FaqQuestionServiceTestParameters extends FaqQuestionTestParameters
    implements PageableTest {

  private final UserServiceTestParameters userServiceTestParameters =
      new UserServiceTestParameters();
  private final FaqCategoryServiceTestParameters faqCategoryServiceTestParameters =
      new FaqCategoryServiceTestParameters();

  public FaqQuestionRequestDto getFaqQuestionRequestDto() {
    return new FaqQuestionRequestDto(
        "Question", "Answer", ACTIVE, UUID.fromString("57ebd52d-6924-4b33-9e48-ce2c68eb9f28"));
  }

  public FaqQuestion getFaqQuestion(UUID userId) {
    FaqQuestionRequestDto faqQuestionRequestDto = getFaqQuestionRequestDto();
    User user = getUserById(userId);
    FaqQuestion faqQuestion =
        new FaqQuestion(
            faqQuestionRequestDto.getQuestion(),
            faqQuestionRequestDto.getAnswer(),
            faqQuestionRequestDto.getStatus(),
            faqCategoryServiceTestParameters.getFaqCategory(userId),
            user,
            user);
    faqQuestion.setId(UUID.fromString("c12bca5d-1ae8-4043-a66a-b55ce799df4c"));

    return faqQuestion;
  }

  public FaqQuestionResponseDto getFaqQuestionResponseDto(FaqQuestion question) {
    User user = question.getUpdatedBy();
    return new FaqQuestionResponseDto(
        question.getId(),
        question.getQuestion(),
        question.getAnswer(),
        question.getStatus(),
        new FaqCategoryOverview(
            question.getCategory().getId(), question.getCategory().getName()),
        new UserOverview(user.getId(), user.getFirstName(), user.getLastName()),
        question.getCreatedAt(),
        question.getUpdatedAt());
  }

  public List<FaqQuestion> getFetchResponse() {
    return List.of(
        getFaqQuestion(UUID.randomUUID()),
        getFaqQuestion(UUID.randomUUID()).toBuilder()
            .question("Question_")
            .answer("Answer_")
            .build());
  }

  public FaqCategory getFaqCategory(UUID userId) {
    return faqCategoryServiceTestParameters.getFaqCategory(userId);
  }

  public List<FaqQuestionResponseDto> getFaqQuestionResponseDtoList(List<FaqQuestion> faqQuestions) {
    return faqQuestions.stream().map(this::getFaqQuestionResponseDto).collect(Collectors.toList());
  }
  public User getUserById(UUID userId) {
    User user = userServiceTestParameters.getSavedUser();
    user.setId(userId);

    return user;
  }
}

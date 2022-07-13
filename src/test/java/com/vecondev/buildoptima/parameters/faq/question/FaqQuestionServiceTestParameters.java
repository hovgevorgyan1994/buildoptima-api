package com.vecondev.buildoptima.parameters.faq.question;

import com.vecondev.buildoptima.dto.request.faq.FaqQuestionRequestDto;
import com.vecondev.buildoptima.dto.response.user.UserOverview;
import com.vecondev.buildoptima.dto.response.faq.FaqCategoryOverview;
import com.vecondev.buildoptima.dto.response.faq.FaqQuestionResponseDto;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import com.vecondev.buildoptima.model.faq.FaqQuestion;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.faq.category.FaqCategoryServiceTestParameters;
import com.vecondev.buildoptima.parameters.user.UserServiceTestParameters;

import java.util.UUID;

import static com.vecondev.buildoptima.model.Status.ACTIVE;

public class FaqQuestionServiceTestParameters {

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

  public FaqQuestionResponseDto getFaqQuestionResponseDto(UUID userId) {
    FaqQuestion faqQuestion = getFaqQuestion(userId);
    User user = faqQuestion.getUpdatedBy();
    return new FaqQuestionResponseDto(
        faqQuestion.getId(),
        faqQuestion.getQuestion(),
        faqQuestion.getAnswer(),
        faqQuestion.getStatus(),
        new FaqCategoryOverview(
            faqQuestion.getCategory().getId(), faqQuestion.getCategory().getName()),
        new UserOverview(user.getId(), user.getFirstName(), user.getLastName()),
        faqQuestion.getCreatedAt(),
        faqQuestion.getUpdatedAt());
  }

  public FaqCategory getFaqCategory(UUID userId) {
    return faqCategoryServiceTestParameters.getFaqCategory(userId);
  }

  public User getUserById(UUID userId) {
    User user = userServiceTestParameters.getSavedUser();
    user.setId(userId);

    return user;
  }
}

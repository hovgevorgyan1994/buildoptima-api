package com.vecondev.buildoptima.parameters.faq.category;

import com.vecondev.buildoptima.dto.request.faq.FaqCategoryRequestDto;
import com.vecondev.buildoptima.dto.response.user.UserOverview;
import com.vecondev.buildoptima.dto.response.faq.FaqCategoryResponseDto;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.user.UserServiceTestParameters;

import java.time.Instant;
import java.util.UUID;

public class FaqCategoryServiceTestParameters {

  private final UserServiceTestParameters userServiceTestParameters =
      new UserServiceTestParameters();

  public FaqCategoryRequestDto getFaqCategoryRequestDto() {
    return new FaqCategoryRequestDto("Royalties");
  }

  public FaqCategory getFaqCategory(UUID userId) {
    User user = getUserById(userId);
    FaqCategory faqCategory =
        new FaqCategory(
            getFaqCategoryRequestDto().getName(), user, user, Instant.now(), Instant.now());
    faqCategory.setId(UUID.fromString("57ebd52d-6924-4b33-9e48-ce2c68eb9f28"));

    return faqCategory;
  }

  public FaqCategoryResponseDto getFaqCategoryResponseDto(UUID userId) {
    FaqCategory faqCategory = getFaqCategory(userId);
    return new FaqCategoryResponseDto(
        faqCategory.getId(),
        faqCategory.getName(),
        new UserOverview(
            userId,
            faqCategory.getUpdatedBy().getFirstName(),
            faqCategory.getUpdatedBy().getLastName()),
        faqCategory.getCreatedAt(),
        faqCategory.getUpdatedAt());
  }

  public User getUserById(UUID userId) {
    User user = userServiceTestParameters.getSavedUser();
    user.setId(userId);

    return user;
  }
}

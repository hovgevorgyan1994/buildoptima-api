package com.vecondev.buildoptima.parameters.faq.category;

import com.vecondev.buildoptima.dto.request.faq.FaqCategoryRequestDto;
import com.vecondev.buildoptima.dto.response.user.UserOverview;
import com.vecondev.buildoptima.dto.response.faq.FaqCategoryResponseDto;
import com.vecondev.buildoptima.parameters.PageableTest;
import com.vecondev.buildoptima.parameters.user.UserServiceTestParameters;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import com.vecondev.buildoptima.model.user.User;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FaqCategoryServiceTestParameters extends FaqCategoryTestParameters
    implements PageableTest {

  private final UserServiceTestParameters userServiceTestParameters =
      new UserServiceTestParameters();

  public FaqCategoryRequestDto getFaqCategoryRequestDto() {
    return new FaqCategoryRequestDto("Royalties");
  }

  public FaqCategory getFaqCategory(UUID userId) {
    User user = getUserById(userId);
    FaqCategory faqCategory = new FaqCategory(getFaqCategoryRequestDto().getName(), user, user);
    faqCategory.setId(UUID.fromString("57ebd52d-6924-4b33-9e48-ce2c68eb9f28"));

    return faqCategory;
  }

  public FaqCategoryResponseDto getFaqCategoryResponseDto(FaqCategory category) {
    return new FaqCategoryResponseDto(
        category.getId(),
        category.getName(),
        new UserOverview(
            category.getUpdatedBy().getId(),
            category.getUpdatedBy().getFirstName(),
            category.getUpdatedBy().getLastName()),
        category.getCreatedAt(),
        category.getUpdatedAt());
  }

  public User getUserById(UUID userId) {
    User user = userServiceTestParameters.getSavedUser();
    user.setId(userId);

    return user;
  }

  public List<FaqCategory> getFetchResponse() {
    return List.of(
        getFaqCategory(UUID.randomUUID()),
        getFaqCategory(UUID.randomUUID()).toBuilder().name("Properties").build());
  }

  public List<FaqCategoryResponseDto> getFaqCategoryResponseDtoList(List<FaqCategory> categories) {
    return categories.stream().map(this::getFaqCategoryResponseDto).collect(Collectors.toList());
  }
}

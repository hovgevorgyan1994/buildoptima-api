package com.vecondev.buildoptima.service.faq.impl;

import com.vecondev.buildoptima.dto.request.faq.FaqCategoryRequestDto;
import com.vecondev.buildoptima.dto.response.faq.FaqCategoryResponseDto;
import com.vecondev.buildoptima.exception.FaqCategoryNotFoundException;
import com.vecondev.buildoptima.mapper.faq.FaqCategoryMapper;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.faq.FaqCategoryRepository;
import com.vecondev.buildoptima.service.faq.FaqCategoryService;
import com.vecondev.buildoptima.service.user.UserService;
import com.vecondev.buildoptima.validation.faq.FaqCategoryValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.vecondev.buildoptima.exception.ErrorCode.FAQ_CATEGORY_NOT_FOUND;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FaqCategoryServiceImpl implements FaqCategoryService {

  private final FaqCategoryMapper faqCategoryMapper;
  private final FaqCategoryRepository faqCategoryRepository;
  private final FaqCategoryValidator faqCategoryValidator;

  private final UserService userService;

  @Override
  public List<FaqCategoryResponseDto> getAllCategories() {
    List<FaqCategory> faqCategories = faqCategoryRepository.findAll();

    return faqCategoryMapper.mapToListDto(faqCategories);
  }

  @Override
  public FaqCategoryResponseDto getCategoryById(UUID categoryId) {
    return faqCategoryMapper.mapToDto(findCategoryById(categoryId));
  }

  @Override
  public FaqCategoryResponseDto createCategory(FaqCategoryRequestDto requestDto, UUID userId) {
    User user = userService.getUserById(userId);
    FaqCategory faqCategory = faqCategoryMapper.mapToEntity(requestDto, user);
    faqCategoryValidator.validateCategoryName(faqCategory.getName());

    faqCategory = faqCategoryRepository.saveAndFlush(faqCategory);
    log.info(
        "User with id: {} created new FAQ Category with name: {}", userId, requestDto.getName());

    return faqCategoryMapper.mapToDto(faqCategory);
  }

  @Override
  public FaqCategoryResponseDto updateCategory(
      UUID categoryId, FaqCategoryRequestDto requestDto, UUID userId) {
    FaqCategory category =
        faqCategoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new FaqCategoryNotFoundException(FAQ_CATEGORY_NOT_FOUND));
    faqCategoryValidator.validateCategoryName(requestDto.getName());

    category = updateFaqCategoryFields(category, requestDto, userId);
    log.info("User with id: {} updated the FAQ Category with id: {}", userId, categoryId);

    return faqCategoryMapper.mapToDto(faqCategoryRepository.saveAndFlush(category));
  }

  @Override
  public void deleteCategory(UUID categoryId, UUID userId) {
    if (!faqCategoryRepository.existsById(categoryId)) {
      throw new FaqCategoryNotFoundException(FAQ_CATEGORY_NOT_FOUND);
    }

    faqCategoryRepository.deleteById(categoryId);
    log.info("User with id: {} deleted the FAQ Category with id: {}", userId, categoryId);
  }

  @Override
  public FaqCategory findCategoryById(UUID id) {
    return faqCategoryRepository
        .findById(id)
        .orElseThrow(() -> new FaqCategoryNotFoundException(FAQ_CATEGORY_NOT_FOUND));
  }

  private FaqCategory updateFaqCategoryFields(
      FaqCategory targetCategory, FaqCategoryRequestDto sourceCategory, UUID userId) {
    return targetCategory.toBuilder()
        .name(sourceCategory.getName())
        .createdBy(userService.getUserById(userId))
        .build();
  }
}

package com.vecondev.buildoptima.service.faq.impl;

import com.vecondev.buildoptima.dto.request.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.faq.FaqCategoryRequestDto;
import com.vecondev.buildoptima.dto.response.FetchResponseDto;
import com.vecondev.buildoptima.dto.response.faq.FaqCategoryResponseDto;
import com.vecondev.buildoptima.exception.FaqCategoryNotFoundException;
import com.vecondev.buildoptima.filter.converter.PageableConverter;
import com.vecondev.buildoptima.filter.model.SortDto;
import com.vecondev.buildoptima.filter.specification.GenericSpecification;
import com.vecondev.buildoptima.repository.faq.FaqCategoryRepository;
import com.vecondev.buildoptima.mapper.faq.FaqCategoryMapper;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.service.faq.FaqCategoryService;
import com.vecondev.buildoptima.service.user.UserService;
import com.vecondev.buildoptima.validation.faq.FaqCategoryValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.vecondev.buildoptima.exception.ErrorCode.FAQ_CATEGORY_NOT_FOUND;
import static com.vecondev.buildoptima.filter.model.FaqCategoryFields.faqCategoryPageSortingFieldsMap;
import static com.vecondev.buildoptima.validation.validator.FieldNameValidator.validateFieldNames;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FaqCategoryServiceImpl implements FaqCategoryService {

  private final FaqCategoryMapper faqCategoryMapper;
  private final FaqCategoryRepository faqCategoryRepository;
  private final FaqCategoryValidator faqCategoryValidator;
  private final PageableConverter pageableConverter;

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

    category =
        category.toBuilder()
            .name(requestDto.getName())
            .createdBy(userService.getUserById(userId))
            .build();
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

  @Override
  public FetchResponseDto fetchCategories(FetchRequestDto fetchRequest) {
    log.info("Request to fetch users from DB");
    validateFieldNames(faqCategoryPageSortingFieldsMap, fetchRequest.getSort());
    if (fetchRequest.getSort() == null || fetchRequest.getSort().isEmpty()) {
      SortDto sortDto = new SortDto("name", SortDto.Direction.ASC);
      fetchRequest.setSort(List.of(sortDto));
    }
    Pageable pageable = pageableConverter.convert(fetchRequest);
    Specification<FaqCategory> specification =
        new GenericSpecification<>(faqCategoryPageSortingFieldsMap, fetchRequest.getFilter());
    Page<FaqCategory> result = faqCategoryRepository.findAll(specification, pageable);

    List<FaqCategoryResponseDto> content = faqCategoryMapper.mapToListDtoFromPage(result);
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

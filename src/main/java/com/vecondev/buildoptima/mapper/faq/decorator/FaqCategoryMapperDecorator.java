package com.vecondev.buildoptima.mapper.faq.decorator;

import com.vecondev.buildoptima.dto.request.faq.FaqCategoryRequestDto;
import com.vecondev.buildoptima.dto.response.faq.FaqCategoryOverview;
import com.vecondev.buildoptima.dto.response.faq.FaqCategoryResponseDto;
import com.vecondev.buildoptima.mapper.faq.FaqCategoryMapper;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import com.vecondev.buildoptima.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;

import java.util.List;

public abstract class FaqCategoryMapperDecorator implements FaqCategoryMapper {

  @Autowired
  @Qualifier("delegate")
  private FaqCategoryMapper faqCategoryMapper;

  @Autowired private UserMapper userMapper;

  @Override
  public FaqCategory mapToEntity(FaqCategoryRequestDto requestDto, User createdBy) {
    return faqCategoryMapper.mapToEntity(requestDto, createdBy).toBuilder()
        .createdBy(createdBy)
        .updatedBy(createdBy)
        .build();
  }

  @Override
  public FaqCategoryResponseDto mapToDto(FaqCategory faqCategory) {
    return faqCategoryMapper.mapToDto(faqCategory).toBuilder()
        .updatedBy(userMapper.mapToOverview(faqCategory.getUpdatedBy()))
        .id(faqCategory.getId())
        .build();
  }

  @Override
  public List<FaqCategoryResponseDto> mapToListDto(List<FaqCategory> categories) {
    return categories.stream().map(this::mapToDto).toList();
  }

  @Override
  public FaqCategoryOverview mapToOverview(FaqCategory faqCategory) {
    return faqCategoryMapper.mapToOverview(faqCategory).toBuilder().id(faqCategory.getId()).build();
  }

  @Override
  public List<FaqCategoryResponseDto> mapToListDtoFromPage(Page<FaqCategory> faqCategories) {
    return faqCategoryMapper.mapToListDtoFromPage(faqCategories);
  }
}

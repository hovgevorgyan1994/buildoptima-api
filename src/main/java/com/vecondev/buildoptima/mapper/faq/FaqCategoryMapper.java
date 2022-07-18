package com.vecondev.buildoptima.mapper.faq;

import com.vecondev.buildoptima.dto.request.faq.FaqCategoryRequestDto;
import com.vecondev.buildoptima.dto.response.faq.FaqCategoryOverview;
import com.vecondev.buildoptima.dto.response.faq.FaqCategoryResponseDto;
import com.vecondev.buildoptima.mapper.faq.decorator.FaqCategoryMapperDecorator;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import com.vecondev.buildoptima.model.user.User;
import org.mapstruct.Context;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@DecoratedWith(FaqCategoryMapperDecorator.class)
public interface FaqCategoryMapper {

    FaqCategory mapToEntity(FaqCategoryRequestDto requestDto, @Context User createdBy);

    @Mapping(target = "updatedBy", ignore = true)
    FaqCategoryResponseDto mapToDto(FaqCategory faqCategory);

    FaqCategoryOverview mapToOverview(FaqCategory faqCategory);

    List<FaqCategoryResponseDto> mapToListDto(List<FaqCategory> categories);
}
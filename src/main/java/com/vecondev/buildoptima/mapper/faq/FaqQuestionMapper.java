package com.vecondev.buildoptima.mapper.faq;

import com.vecondev.buildoptima.dto.request.faq.FaqQuestionRequestDto;
import com.vecondev.buildoptima.dto.response.faq.FaqQuestionResponseDto;
import com.vecondev.buildoptima.mapper.faq.decorator.FaqQuestionMapperDecorator;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import com.vecondev.buildoptima.model.faq.FaqQuestion;
import com.vecondev.buildoptima.model.user.User;
import org.mapstruct.Context;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@DecoratedWith(FaqQuestionMapperDecorator.class)
public interface FaqQuestionMapper {

  FaqQuestion mapToEntity(
      FaqQuestionRequestDto requestDto, @Context FaqCategory faqCategory, @Context User createdBy);

  FaqQuestionResponseDto mapToDto(FaqQuestion question);

  List<FaqQuestionResponseDto> mapToListDto(List<FaqQuestion> faqQuestions);
}

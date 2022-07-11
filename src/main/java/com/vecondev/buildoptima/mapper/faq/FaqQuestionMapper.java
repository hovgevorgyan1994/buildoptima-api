package com.vecondev.buildoptima.mapper.faq;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@DecoratedWith(FaqQuestionMapperDecorator.class)
public interface FaqQuestionMapper {}

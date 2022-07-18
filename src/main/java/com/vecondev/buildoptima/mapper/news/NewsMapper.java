package com.vecondev.buildoptima.mapper.news;

import com.vecondev.buildoptima.dto.request.news.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.response.news.NewsResponseDto;
import com.vecondev.buildoptima.model.news.News;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@DecoratedWith(NewsMapperDecorator.class)
public interface NewsMapper {

  @Mapping(target = "keywords", ignore = true)
  News mapToEntity(NewsCreateRequestDto dto);

  @Mapping(target = "keywords", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  NewsResponseDto mapToResponseDto(News news);

  @Mapping(target = "keywords", ignore = true)
  List<NewsResponseDto> mapToResponseList(Page<News> newsPage);
}
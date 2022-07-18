package com.vecondev.buildoptima.mapper.news;

import com.vecondev.buildoptima.dto.request.news.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.response.news.NewsResponseDto;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.Status;
import com.vecondev.buildoptima.model.news.News;
import com.vecondev.buildoptima.model.news.NewsCategory;
import com.vecondev.buildoptima.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;

import java.util.Arrays;
import java.util.List;

public abstract class NewsMapperDecorator implements NewsMapper {

  @Autowired
  @Qualifier("delegate")
  private NewsMapper mapper;

  @Autowired private UserMapper userMapper;
  @Autowired private UserRepository userRepository;

  @Override
  public News mapToEntity(NewsCreateRequestDto dto) {
    News news =
        mapper.mapToEntity(dto).toBuilder()
            .category(NewsCategory.valueOf(dto.getCategory()))
            .status(Status.ACTIVE)
            .build();
    StringBuilder keywords = new StringBuilder();
    dto.getKeywords().forEach(keyword -> keywords.append(keyword).append(" "));
    news.setKeywords(keywords.toString());
    return news;
  }

  @Override
  public NewsResponseDto mapToResponseDto(News news) {
    NewsResponseDto responseDto = mapper.mapToResponseDto(news);
    String keywordsAsString = news.getKeywords();
    if (keywordsAsString != null) {
      List<String> keywords = Arrays.stream(keywordsAsString.split(" ")).toList();
      responseDto.setKeywords(keywords);
    }
    responseDto.setCreatedBy(
        userMapper.mapToResponseDto(userRepository.getReferenceById(news.getCreatedBy())));
    if (news.getUpdatedBy() != null) {
      responseDto.setUpdatedBy(
          userMapper.mapToResponseDto(userRepository.getReferenceById(news.getUpdatedBy())));
    }
    return responseDto;
  }

  @Override
  public List<NewsResponseDto> mapToResponseList(Page<News> newsPage) {
    return newsPage.getContent().stream().map(this::mapToResponseDto).toList();
  }
}

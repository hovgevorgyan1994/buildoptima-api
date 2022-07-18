package com.vecondev.buildoptima.parameters.news;

import com.vecondev.buildoptima.dto.request.news.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsUpdateRequestDto;
import com.vecondev.buildoptima.dto.response.news.NewsResponseDto;
import com.vecondev.buildoptima.model.Status;
import com.vecondev.buildoptima.model.news.News;
import com.vecondev.buildoptima.model.news.NewsCategory;
import com.vecondev.buildoptima.util.TestUtil;

import java.time.Instant;

public class NewsServiceTestParameters extends TestUtil {

  public NewsCreateRequestDto getCreateNewsRequestDto() {
    return NewsCreateRequestDto.builder()
        .title("Summer Sales")
        .summary("Steam Summer Sale 2022 continues — save big on top rated PC games")
        .description("stringstringstringstringstringstringstringstringst")
        .category("BREAKING_NEWS")
        .build();
  }

  public NewsUpdateRequestDto getUpdateNewsRequestDto() {
    return NewsUpdateRequestDto.builder()
        .title("Winter Sales")
        .summary("Steam Summer Sale 2022 continues — save big on top rated PC games")
        .description("stringstringstringstringstringstringstringstringst")
        .category("OPINION")
        .build();
  }

  public News getNewsFromCreateNewsDto(NewsCreateRequestDto dto) {
    News news =
        News.builder()
            .title(dto.getTitle())
            .summary(dto.getSummary())
            .description(dto.getDescription())
            .category(NewsCategory.valueOf(dto.getCategory()))
            .status(Status.ACTIVE)
            .build();
    news.setCreatedAt(Instant.now());
    return news;
  }

  public NewsResponseDto getNewsResponseDto(News news) {
    return NewsResponseDto.builder()
        .id(news.getId())
        .title(news.getTitle())
        .summary(news.getSummary())
        .description(news.getDescription())
        .category(news.getCategory())
        .createdBy(
            String.format(
                "%s %s", news.getCreatedBy().getFirstName(), news.getCreatedBy().getLastName()))
        .createdAt(Instant.now())
        .build();
  }
}

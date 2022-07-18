package com.vecondev.buildoptima.parameters.news;

import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsUpdateRequestDto;
import com.vecondev.buildoptima.dto.response.news.NewsResponseDto;
import com.vecondev.buildoptima.filter.model.Criteria;
import com.vecondev.buildoptima.filter.model.SortDto;
import com.vecondev.buildoptima.model.Status;
import com.vecondev.buildoptima.model.news.News;
import com.vecondev.buildoptima.model.news.NewsCategory;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.user.UserServiceTestParameters;
import com.vecondev.buildoptima.util.TestUtil;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.vecondev.buildoptima.filter.model.SearchOperation.EQ;
import static com.vecondev.buildoptima.filter.model.SearchOperation.GT;
import static com.vecondev.buildoptima.filter.model.SearchOperation.LIKE;

public class NewsServiceTestParameters extends TestUtil {

  private final UserServiceTestParameters userServiceTestParameters =
      new UserServiceTestParameters();

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
            userServiceTestParameters.getUserResponseDto(userServiceTestParameters.getSavedUser()))
        .createdAt(Instant.now())
        .build();
  }

  public List<News> getFetchResponse () {
    return List.of(
            News.builder().title("Summer Sales").summary("Summer Sales").build(),
            News.builder().title("Winter Sales").summary("Winter Sales").build());
  }

  public List<NewsResponseDto> getNewsResponseDtoList (List<News> news) {
    return news.stream().map(this::getNewsResponseDto).collect(Collectors.toList());

  }

  public FetchRequestDto getFetchRequest () {
    return new FetchRequestDto(
            0,
            10,
            List.of(new SortDto("title", SortDto.Direction.ASC)),
            Map.of(
                    "and",
                    List.of(
                            new Criteria(LIKE, "title", "Summer"),
                            Map.of(
                                    "or",
                                    List.of(
                                            new Criteria(GT, "createdAt", "2018-11-30T18:35:24.00Z"))))));

  }
}

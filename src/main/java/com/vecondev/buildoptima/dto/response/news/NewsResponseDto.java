package com.vecondev.buildoptima.dto.response.news;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vecondev.buildoptima.dto.response.user.UserResponseDto;
import com.vecondev.buildoptima.model.news.NewsCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponseDto {

  private UUID id;
  private String title;
  private String summary;
  private String description;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> keywords;
  private NewsCategory category;
  private Instant createdAt;
  private Instant updatedAt;
  private UserResponseDto createdBy;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private UserResponseDto updatedBy;
}
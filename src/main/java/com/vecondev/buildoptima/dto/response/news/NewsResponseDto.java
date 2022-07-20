package com.vecondev.buildoptima.dto.response.news;

import com.vecondev.buildoptima.dto.EntityOverview;
import com.vecondev.buildoptima.model.Status;
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
  private List<String> keywords;
  private NewsCategory category;
  private Status status;
  private Instant createdAt;
  private Instant updatedAt;
  private EntityOverview createdBy;
  private EntityOverview updatedBy;
}

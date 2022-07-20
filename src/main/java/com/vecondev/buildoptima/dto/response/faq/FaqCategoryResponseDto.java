package com.vecondev.buildoptima.dto.response.faq;

import com.vecondev.buildoptima.dto.EntityOverview;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@Schema(name = "FAQ Category Response DTO")
public class FaqCategoryResponseDto {

  @Schema(example = "2635b586-d0d7-4a2d-b4b5-c98377a02322")
  private UUID id;

  @Schema(example = "Royalties & Statements")
  private String name;

  private EntityOverview updatedBy;

  @Schema(example = "2022-06-24 13:29:00.887950")
  private Instant createdAt;

  @Schema(example = "2022-06-24 13:29:00.887950")
  private Instant updatedAt;
}

package com.vecondev.buildoptima.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@Schema(name = "Image Overview")
public class ImageOverview {

  @Schema(example = "2635b586-d0d7-4a2d-b4b5-c98377a02322")
  private UUID id;

  @Schema(example = "2")
  private Integer imageVersion;
}

package com.vecondev.buildoptima.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Fetch Response DTO")
public class FetchResponseDto {

  private List<?> content;

  private int page;

  private int size;

  private long totalElements;

  private boolean last;
}

package com.vecondev.buildoptima.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.vecondev.buildoptima.constant.FetchConstants.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Fetch Request DTO")
public class FetchRequestDto {

  private int page = DEFAULT_PAGE_NUMBER;

  private int size = DEFAULT_PAGE_SIZE;

  private String sortBy = DEFAULT_SORT_BY;

  private String sortDir = DEFAULT_SORT_DIRECTION;
}

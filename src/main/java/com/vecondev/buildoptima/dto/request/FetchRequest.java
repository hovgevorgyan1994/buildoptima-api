package com.vecondev.buildoptima.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.vecondev.buildoptima.constant.FetchConstants.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchRequest {

  private int page = DEFAULT_PAGE_NUMBER;

  private int size = DEFAULT_PAGE_SIZE;

  private String sortBy = DEFAULT_SORT_BY;

  private String sortDir = DEFAULT_SORT_DIRECTION;
}

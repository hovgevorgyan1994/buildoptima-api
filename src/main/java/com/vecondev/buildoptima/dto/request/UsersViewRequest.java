package com.vecondev.buildoptima.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.vecondev.buildoptima.util.AppConstants.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsersViewRequest {

  private int pageNo = DEFAULT_PAGE_NUMBER;

  private int pageSize = DEFAULT_PAGE_SIZE;

  private String sortBy = DEFAULT_SORT_BY;

  private String sortDir = DEFAULT_SORT_DIRECTION;
}

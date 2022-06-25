package com.vecondev.buildoptima.dto.request;

import com.vecondev.buildoptima.filter.model.SortDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "FetchRequest")
public class FetchRequestDto {

  private Integer skip;

  private Integer take;

  private List<@Valid SortDto> sort;

  Map<String, Object> filter;
}

package com.vecondev.buildoptima.dto.filter;

import com.vecondev.buildoptima.filter.model.SortDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Fetch Request DTO")
public class FetchRequestDto {

  Map<String, Object> filter;
  private Integer skip;
  private Integer take;
  private List<@Valid SortDto> sort;
}

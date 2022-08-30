package com.vecondev.buildoptima.dto.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyListDto {

  private List<PropertyReadDto> properties;
}

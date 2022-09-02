package com.vecondev.buildoptima.parameters.property;

import com.vecondev.buildoptima.dto.property.response.PropertyResponseDto;
import com.vecondev.buildoptima.model.property.Property;

public class PropertyServiceTestParameters {

  public Property getByAin() {
    return Property.builder().ain("123456").build();
  }

  public PropertyResponseDto mapToResponseDto() {
    return PropertyResponseDto.builder().ain("123456").build();
  }
}

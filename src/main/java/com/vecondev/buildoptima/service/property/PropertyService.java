package com.vecondev.buildoptima.service.property;

import com.vecondev.buildoptima.dto.property.response.PropertyOverview;
import com.vecondev.buildoptima.dto.property.response.PropertyResponseDto;
import com.vecondev.buildoptima.filter.model.PropertySearchCriteria;
import java.util.List;

public interface PropertyService {

  List<PropertyOverview> search(String value, PropertySearchCriteria criteria);

  PropertyResponseDto getByAin(String ain);
}

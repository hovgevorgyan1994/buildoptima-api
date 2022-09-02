package com.vecondev.buildoptima.mapper.property;

import com.vecondev.buildoptima.dto.property.PropertyReadDto;
import com.vecondev.buildoptima.dto.property.response.PropertyOverview;
import com.vecondev.buildoptima.dto.property.response.PropertyResponseDto;
import com.vecondev.buildoptima.model.property.Property;
import java.util.List;
import java.util.Map;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.opensearch.search.SearchHit;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@DecoratedWith(PropertyMapperDecorator.class)
public interface PropertyMapper {

  @Mapping(target = "locations", ignore = true)
  Property mapToEntity(PropertyReadDto dto);

  List<Property> mapToEntityList(List<PropertyReadDto> list);

  @Mapping(target = "addresses", ignore = true)
  PropertyResponseDto mapToResponseDto(Property property);

  default PropertyOverview mapToOverview(SearchHit searchHit) {
    Map<String, Object> fields = searchHit.getSourceAsMap();
    return new PropertyOverview(
        fields.get("property_ain").toString(), fields.get("address_to_display").toString());
  }
}

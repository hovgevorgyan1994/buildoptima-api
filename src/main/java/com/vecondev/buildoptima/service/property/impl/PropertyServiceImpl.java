package com.vecondev.buildoptima.service.property.impl;

import static com.vecondev.buildoptima.filter.model.PropertySearchCriteria.ADDRESS;

import com.vecondev.buildoptima.dto.property.response.PropertyOverview;
import com.vecondev.buildoptima.dto.property.response.PropertyResponseDto;
import com.vecondev.buildoptima.exception.Error;
import com.vecondev.buildoptima.exception.ResourceNotFoundException;
import com.vecondev.buildoptima.filter.model.PropertySearchCriteria;
import com.vecondev.buildoptima.mapper.property.PropertyMapper;
import com.vecondev.buildoptima.repository.property.PropertyRepository;
import com.vecondev.buildoptima.service.opensearch.OpenSearchService;
import com.vecondev.buildoptima.service.property.PropertyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.search.SearchHit;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

  private static final int MAX_COUNT_BY_SEARCH = 10;

  private final OpenSearchService openSearchService;
  private final PropertyRepository propertyRepository;
  private final PropertyMapper propertyMapper;

  /**
   * Search through properties by either address or ain.
   *
   * @param value the value to search by
   * @param criteria the parameter showing by which criteria ('address' or 'ain') should the search
   *     take place
   * @return the search results that can be up to 10
   */
  @Override
  public List<PropertyOverview> search(String value, PropertySearchCriteria criteria) {
    List<SearchHit> hits =
        (criteria == ADDRESS)
            ? openSearchService.searchByAddress(value, MAX_COUNT_BY_SEARCH)
            : openSearchService.searchByAin(value, MAX_COUNT_BY_SEARCH);

    return hits.stream().map(propertyMapper::mapToOverview).toList();
  }

  @Override
  public PropertyResponseDto getByAin(String ain) {
    return propertyMapper.mapToResponseDto(
        propertyRepository
            .findById(ain)
            .orElseThrow(() -> new ResourceNotFoundException(Error.PROPERTY_NOT_FOUND)));
  }


}

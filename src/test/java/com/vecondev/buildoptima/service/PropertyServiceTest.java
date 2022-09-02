package com.vecondev.buildoptima.service;

import static com.vecondev.buildoptima.filter.model.PropertySearchCriteria.ADDRESS;
import static com.vecondev.buildoptima.filter.model.PropertySearchCriteria.AIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vecondev.buildoptima.dto.property.response.PropertyResponseDto;
import com.vecondev.buildoptima.exception.ResourceNotFoundException;
import com.vecondev.buildoptima.mapper.property.PropertyMapper;
import com.vecondev.buildoptima.model.property.Property;
import com.vecondev.buildoptima.parameters.property.PropertyServiceTestParameters;
import com.vecondev.buildoptima.repository.property.PropertyRepository;
import com.vecondev.buildoptima.service.opensearch.OpenSearchService;
import com.vecondev.buildoptima.service.property.impl.PropertyServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class PropertyServiceTest {

  private static final int MAX_COUNT_BY_SEARCH = 10;
  private final PropertyServiceTestParameters testParameters = new PropertyServiceTestParameters();
  @InjectMocks private PropertyServiceImpl propertyService;
  @Mock private OpenSearchService openSearchService;
  @Mock private PropertyRepository propertyRepository;
  @Mock private PropertyMapper propertyMapper;

  @Test
  void getPropertyByAinSuccess() {
    String ain = "123456";
    Property property = testParameters.getByAin();
    PropertyResponseDto responseDto = testParameters.mapToResponseDto();

    when(propertyRepository.findById(ain)).thenReturn(Optional.of(property));
    when(propertyMapper.mapToResponseDto(any(Property.class))).thenReturn(responseDto);

    PropertyResponseDto propertyResponseDto = propertyService.getByAin(ain);
    assertNotNull(propertyResponseDto);
    assertEquals(propertyResponseDto.getAin(), ain);
  }

  @Test
  void getPropertyByAinFailedAsPropertyNotFound() {
    String ain = "123456";

    doThrow(ResourceNotFoundException.class).when(propertyRepository).findById(ain);

    assertThrows(ResourceNotFoundException.class, () -> propertyService.getByAin(ain));
  }

  @Test
  void successfulSearchByAddress() {
    String value = "10424";

    propertyService.search(value, ADDRESS);

    verify(openSearchService).searchByAddress(value, MAX_COUNT_BY_SEARCH);
  }

  @Test
  void successfulSearchByAin() {
    String value = "605102";

    propertyService.search(value, AIN);

    verify(openSearchService).searchByAin(value, MAX_COUNT_BY_SEARCH);
  }
}

package com.vecondev.buildoptima.api.controller;

import com.vecondev.buildoptima.api.PropertyApi;
import com.vecondev.buildoptima.dto.property.response.PropertyOverview;
import com.vecondev.buildoptima.dto.property.response.PropertyResponseDto;
import com.vecondev.buildoptima.filter.model.PropertySearchCriteria;
import com.vecondev.buildoptima.service.property.PropertyService;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties")
public class PropertyController implements PropertyApi {

  private final PropertyService propertyService;

  @GetMapping("/search")
  public ResponseEntity<List<PropertyOverview>> search(
      @NotNull @RequestParam String value,
      @NotNull @RequestParam("by") PropertySearchCriteria propertySearchCriteria) {
    return ResponseEntity.ok(propertyService.search(value, propertySearchCriteria));
  }

  @Override
  @GetMapping("/{ain}")
  public ResponseEntity<PropertyResponseDto> getByAin(@PathVariable String ain) {
    return ResponseEntity.ok(propertyService.getByAin(ain));
  }
}

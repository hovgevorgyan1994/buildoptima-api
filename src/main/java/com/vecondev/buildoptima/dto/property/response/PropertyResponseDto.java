package com.vecondev.buildoptima.dto.property.response;

import com.vecondev.buildoptima.dto.property.AddressDto;
import com.vecondev.buildoptima.model.property.Details;
import com.vecondev.buildoptima.model.property.Hazards;
import com.vecondev.buildoptima.model.property.Locations;
import com.vecondev.buildoptima.model.property.ZoningDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Property Response DTO")
public class PropertyResponseDto {

  private String ain;
  private String municipality;
  private List<AddressDto> addresses;
  private Locations locations;
  private Details details;
  private Hazards hazards;
  private ZoningDetails zoningDetails;
  private Integer version;
  private Instant createdAt;
  private Instant updatedAt;
}

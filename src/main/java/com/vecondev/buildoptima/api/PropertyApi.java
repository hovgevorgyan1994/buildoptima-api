package com.vecondev.buildoptima.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.vecondev.buildoptima.dto.property.response.PropertyOverview;
import com.vecondev.buildoptima.dto.property.response.PropertyResponseDto;
import com.vecondev.buildoptima.filter.model.PropertySearchCriteria;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(
    name = "Property",
    description = "Endpoints for managing properties",
    externalDocs =
    @ExternalDocumentation(
        description = "Click here to see a detailed explanation of application errors",
        url =
            "https://github.com/vecondev/buildoptima-api/blob/develop/docs/application-errors.md"))
public interface PropertyApi {

  @Operation(
      summary = "Search through properties by address or ain",
      description = """
                    Each request can return up to 10 property info that are 
                    more similar to the search criteria than other ones. 
                    Possible error codes: 50014""")
  @ApiResponses(
      value = {
          @ApiResponse(
              responseCode = "200",
              description = "The search has been successfully done",
              content =
              @Content(schema = @Schema(implementation = PropertyOverview.class),
                        mediaType = APPLICATION_JSON_VALUE)),
      })
  ResponseEntity<List<PropertyOverview>> search(
      @Parameter(description = "The search criteria") String value,
      @Parameter(description = """
                               Parameter to mention in which criteria
                               ('address' or 'ain') to search by""")
      PropertySearchCriteria propertySearchCriteria);

  @Operation(
      summary = "Get property data by ain",
      description = "Possible error codes: 4011, 4012, 4013, 4014, 4031, 4049")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved property data by ain",
            content =
                @Content(
                    schema = @Schema(implementation = PropertyResponseDto.class),
                    mediaType = APPLICATION_JSON_VALUE))
      })
  ResponseEntity<PropertyResponseDto> getByAin(
      @Parameter(description = "The property data ain which should be fetched") String ain);
}

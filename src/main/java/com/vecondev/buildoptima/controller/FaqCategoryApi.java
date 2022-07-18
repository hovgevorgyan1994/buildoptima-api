package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.dto.request.faq.FaqCategoryRequestDto;
import com.vecondev.buildoptima.dto.response.faq.FaqCategoryResponseDto;
import com.vecondev.buildoptima.exception.ApiError;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "FAQ Category", description = "Endpoints for managing FAQ categories")
public interface FaqCategoryApi {

  @Operation(
      summary = "Get all FAQ categories",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "All FAQ Categories should be retrieved",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = FaqCategoryResponseDto.class))),
        @ApiResponse(
            responseCode = "403",
            description =
                "The access token is either not provided or invalid, or authenticated user hasn't permission to delete this resource",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<List<FaqCategoryResponseDto>> getAllCategories(
      @Parameter(hidden = true) AppUserDetails authenticatedUser);

  @Operation(
      summary = "Get FAQ category by id",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The FAQ Category should be retrieved",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = FaqCategoryResponseDto.class))),
        @ApiResponse(
            responseCode = "403",
            description =
                "The access token is either not provided or invalid, or authenticated user hasn't permission to delete this resource",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "There is no FAQ Category found with such Id",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<FaqCategoryResponseDto> getCategoryById(
      UUID id, @Parameter(hidden = true) AppUserDetails authenticatedUser);

  @Operation(
      summary = "Create new FAQ category",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "New FAQ category should be created.",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = FaqCategoryResponseDto.class))),
        @ApiResponse(
            responseCode = "400",
            ref = "#/components/responses/methodArgumentNotValidResponse"),
        @ApiResponse(
            responseCode = "403",
            description =
                "The access token is either not provided or invalid, or authenticated user hasn't permission to delete this resource",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "409",
            description = "There already is an duplicate value for category name.",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<FaqCategoryResponseDto> createCategory(
      FaqCategoryRequestDto requestDto, @Parameter(hidden = true) AppUserDetails authenticatedUser);

  @Operation(
      summary = "Update the FAQ category",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The FAQ Category should be updated",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = FaqCategoryResponseDto.class))),
        @ApiResponse(
            responseCode = "400",
            ref = "#/components/responses/methodArgumentNotValidResponse"),
        @ApiResponse(
            responseCode = "403",
            description =
                "The access token is either not provided or invalid, or authenticated user hasn't permission to delete this resource",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "There is no FAQ Category found with such Id",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "409",
            description = "There already is an duplicate value for category name.",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<FaqCategoryResponseDto> updateCategory(
      UUID id,
      FaqCategoryRequestDto requestDto,
      @Parameter(hidden = true) AppUserDetails authenticatedUser);

  @Operation(
      summary = "Delete the FAQ category",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The FAQ Category should be deleted",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = FaqCategoryResponseDto.class))),
        @ApiResponse(
            responseCode = "403",
            description =
                "The access token is either not provided or invalid, or authenticated user hasn't permission to delete this resource",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "There is no FAQ Category found with such Id",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<Void> deleteCategory(
      UUID id, @Parameter(hidden = true) AppUserDetails authenticatedUser);
}
package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.dto.request.faq.FaqQuestionRequestDto;
import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.response.faq.FaqCategoryResponseDto;
import com.vecondev.buildoptima.dto.response.faq.FaqQuestionResponseDto;
import com.vecondev.buildoptima.dto.response.filter.FetchResponseDto;
import com.vecondev.buildoptima.exception.ApiError;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "FAQ Question", description = "Endpoints for managing FAQ questions")
public interface FaqQuestionApi {

  @Operation(
      summary = "Get all FAQ Questions",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The FAQ Questions should be retrieved",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = FaqQuestionResponseDto.class))),
        @ApiResponse(
            responseCode = "403",
            description =
                "Authenticated user hasn't permission to get these resources (Should be either MODERATOR or ADMIN)",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<List<FaqQuestionResponseDto>> getAllQuestions(
      @Parameter(hidden = true) AppUserDetails authenticatedUser);

  @Operation(
      summary = "Get FAQ Question by id",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The FAQ Question should be got",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = FaqCategoryResponseDto.class))),
        @ApiResponse(
            responseCode = "403",
            description =
                "Authenticated user hasn't permission to get such resource (Should be either MODERATOR or ADMIN)",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "There is no FAQ Question found with such Id",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<FaqQuestionResponseDto> getQuestionById(
      @PathVariable UUID id, @Parameter(hidden = true) AppUserDetails authenticatedUser);

  @Operation(
      summary = "Create new FAQ Question",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "New FAQ Question should be created.",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = FaqQuestionResponseDto.class))),
        @ApiResponse(
            responseCode = "400",
            ref = "#/components/responses/methodArgumentNotValidResponse"),
        @ApiResponse(
            responseCode = "403",
            description =
                "Authenticated user hasn't permission to create such resource (Should be either MODERATOR or ADMIN)",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "There is no category found with such id.",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "409",
            description = "There already is an duplicate value for the question.",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<FaqQuestionResponseDto> createQuestion(
      FaqQuestionRequestDto requestDto, @Parameter(hidden = true) AppUserDetails authenticatedUser);

  @Operation(
      summary = "Update the FAQ Question",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "New FAQ Question should be created.",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = FaqQuestionResponseDto.class))),
        @ApiResponse(
            responseCode = "400",
            ref = "#/components/responses/methodArgumentNotValidResponse"),
        @ApiResponse(
            responseCode = "403",
            description =
                "Authenticated user hasn't permission to update this resource (Should be either MODERATOR or ADMIN)",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "There is no category found with such id.",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "409",
            description = "There already is an duplicate value for the question.",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<FaqQuestionResponseDto> updateQuestion(
      UUID id,
      FaqQuestionRequestDto requestDto,
      @Parameter(hidden = true) AppUserDetails authenticatedUser);

  @Operation(
      summary = "Delete the FAQ Question",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The FAQ Question should be deleted",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = FaqCategoryResponseDto.class))),
        @ApiResponse(
            responseCode = "403",
            description =
                "Authenticated user hasn't permission to delete this resource (Should be either MODERATOR or ADMIN)",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "There is no FAQ Question found with such Id",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<Void> deleteQuestion(
      @PathVariable UUID id, @Parameter(hidden = true) AppUserDetails authenticatedUser);

  @Operation(
      summary = "Fetch FAQ Questions sorted, filtered and paged",
      security = @SecurityRequirement(name = "api-security"),
      externalDocs =
          @ExternalDocumentation(
              description =
                  "Click here to see a detailed explanation of this endpoint requirements",
              url =
                  "https://github.com/vecondev/buildoptima-api/blob/develop/docs/faq_question-filter-sorting.md"))
  @RequestBody(ref = "#/components/requestBodies/fetchFaqQuestionsRequestExample")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The FAQ questions should be retrieved regarding given criteria",
            content =
                @Content(
                    schema = @Schema(implementation = FetchResponseDto.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "400",
            description = "There is an invalid value in request body",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "403",
            description =
                "Authenticated user hasn't permission to get these resources (Should be either MODERATOR or ADMIN)",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE))
      })
  ResponseEntity<FetchResponseDto> fetchQuestions(FetchRequestDto fetchRequest);
}

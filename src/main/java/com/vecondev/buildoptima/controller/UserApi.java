package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.dto.request.user.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.user.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.user.ConfirmEmailRequestDto;
import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.user.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.request.user.RestorePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.user.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.response.user.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.response.user.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.user.UserResponseDto;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "User", description = "Endpoints for managing users")
public interface UserApi {

  @Operation(summary = "Register new user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "New user should be registered.",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserResponseDto.class))),
        @ApiResponse(
            responseCode = "400",
            ref = "#/components/responses/methodArgumentNotValidResponse"),
        @ApiResponse(
            responseCode = "409",
            description = "There already is an duplicate value either for email or phone number.",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<UserResponseDto> register(
      UserRegistrationRequestDto userRegistrationRequestDto, Locale locale);

  @Operation(summary = "Activate email")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User account activated by email confirmation",
            content =
                @Content(
                    schema = @Schema(implementation = UserResponseDto.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "404",
            description = "Email confirmation token is not found",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE))
      })
  ResponseEntity<UserResponseDto> activate(String token);

  @Operation(summary = "User sign in")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User signed in",
            content =
                @Content(
                    schema = @Schema(implementation = AuthResponseDto.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "400",
            ref = "#/components/responses/methodArgumentNotValidResponse"),
        @ApiResponse(
            responseCode = "401",
            description = "Bad credentials",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE))
      })
  ResponseEntity<AuthResponseDto> login(AuthRequestDto authRequestDto);

  @Operation(summary = "Refresh user token")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "New access token was created for user",
            content =
                @Content(
                    schema = @Schema(implementation = RefreshTokenResponseDto.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid refresh token was sent",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "403",
            description = "Expired refresh token was sent",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "404",
            description = "Credentials not found",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE))
      })
  ResponseEntity<RefreshTokenResponseDto> refreshToken(
      RefreshTokenRequestDto refreshTokenRequestDto);

  @Operation(
      summary = "Fetch users sorted paged and sorted: FOR ADMIN ONLY",
      security = @SecurityRequirement(name = "api-security"),
      externalDocs =
          @ExternalDocumentation(
              description =
                  "Click here to see a detailed explanation of this endpoint requirements",
              url =
                  "https://github.com/vecondev/buildoptima-api/blob/develop/docs/user-filter-sorting.md"))
  @RequestBody(ref = "#/components/requestBodies/fetchUsersRequestExample")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Fetched users from DB",
            content =
                @Content(
                    schema = @Schema(implementation = FetchResponseDto.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized request",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "400",
            description = "Expired access token or there is an invalid value in request body",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE))
      })
  ResponseEntity<FetchResponseDto> fetchUsers(@RequestBody FetchRequestDto viewRequest);

  @Operation(
      summary = "Change user password",
      security = @SecurityRequirement(name = "api-security"))
  @RequestBody(
      content =
          @Content(
              schema = @Schema(implementation = ChangePasswordRequestDto.class),
              mediaType = APPLICATION_JSON_VALUE))
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "User password was successfully changed"),
        @ApiResponse(
            responseCode = "400",
            ref = "#/components/responses/methodArgumentNotValidResponse"),
        @ApiResponse(responseCode = "401", description = "Unauthorized request"),
        @ApiResponse(responseCode = "403", description = "Expired access token"),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "409",
            description = "Provided The Same Password In Change Password Request",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "406",
            description = "Provided Wrong Password In Change Password Request",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE))
      })
  ResponseEntity<Void> changePassword(ChangePasswordRequestDto request, AppUserDetails userDetails);

  @Operation(
      summary = "Get user profile",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Fetched a user from DB",
            content =
                @Content(
                    schema = @Schema(implementation = UserResponseDto.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized request",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "400", description = "Expired access token"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE))
      })
  ResponseEntity<UserResponseDto> getUser(@PathVariable("id") UUID userId);

  @Operation(summary = "Request to receive an email to restore the password")
  @RequestBody(
      content =
          @Content(
              schema = @Schema(implementation = ConfirmEmailRequestDto.class),
              mediaType = APPLICATION_JSON_VALUE))
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Email was sent to user"),
        @ApiResponse(
            responseCode = "500",
            description = "Failed to send Email",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE)),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE))
      })
  ResponseEntity<Void> verifyEmail (ConfirmEmailRequestDto email, Locale locale);

  @Operation(summary = "Restore forgotten password")
  @RequestBody(
      content =
          @Content(
              schema = @Schema(implementation = RestorePasswordRequestDto.class),
              mediaType = APPLICATION_JSON_VALUE))
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "User password successfully changed"),
        @ApiResponse(
            responseCode = "400",
            ref = "#/components/responses/methodArgumentNotValidResponse"),
        @ApiResponse(
            responseCode = "404",
            description = "Confirmation token not found",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = APPLICATION_JSON_VALUE))
      })
  ResponseEntity<Void> restorePassword(RestorePasswordRequestDto restorePasswordRequestDto);

  @Operation(
      summary = "Upload new image or updates the previous one of given user",
      description =
          "The image has following requirements (extension: jpeg/jpg/png, min_width: 600px, max_width: 600px, size: 70KB-30MB)",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "The image is successfully uploaded/updated"),
        @ApiResponse(
            responseCode = "403",
            description =
                "The access token is either not provided or invalid, or authenticated user hasn't permission to perform this action",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "412",
            description =
                "Image is not provided or the provided image  doesn't fit the requirements",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
      })
  ResponseEntity<Void> uploadImage(
      @Parameter(description = "The user's id whom photo should be uploaded") UUID id,
      @Parameter(hidden = true) AppUserDetails user,
      @Parameter(description = "The image user want to upload") MultipartFile multipartFile);

  @Operation(
      summary = "Download the original image of given user",
      description =
          "The permission to download the image has only the resource owner and the admin",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The image is successfully downloaded",
            content = {
              @Content(mediaType = MediaType.IMAGE_JPEG_VALUE),
              @Content(mediaType = MediaType.IMAGE_PNG_VALUE),
              @Content(mediaType = APPLICATION_JSON_VALUE)
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "The access token is either not provided or invalid, or authenticated user hasn't permission to get this resource",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "No image found by given user",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<byte[]> downloadOriginalImage(
      @Parameter(hidden = true) AppUserDetails user,
      @Parameter(description = "The user id whom image should be downloaded") UUID ownerId);

  @Operation(
      summary = "Download the thumbnail image by given id",
      description =
          "The permission to download the image has only the resource owner and the admin",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The image is successfully downloaded",
            content = {
              @Content(mediaType = MediaType.IMAGE_JPEG_VALUE),
              @Content(mediaType = MediaType.IMAGE_PNG_VALUE),
              @Content(mediaType = APPLICATION_JSON_VALUE)
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "The access token is either not provided or invalid, or authenticated user hasn't permission to get this resource",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "No image found by given user",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<byte[]> downloadThumbnailImage(
      @Parameter(hidden = true) AppUserDetails user,
      @Parameter(description = "The user id whom image should be downloaded") UUID ownerId);

  @Operation(
      summary = "Delete the images (original, thumbnail) of given user",
      description =
          "The permission to download the image has only the resource owner and the admin",
      security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "The image is successfully deleted"),
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
            description = "No image found by given user",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<Void> deleteImage(
      @Parameter(hidden = true) AppUserDetails user,
      @Parameter(description = "The user id whom image should be deleted") UUID ownerId);
}

package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.dto.request.*;
import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.FetchResponseDto;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.UserResponseDto;
import com.vecondev.buildoptima.error.ApiError;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;

import java.util.Locale;
import java.util.UUID;

public interface UserApi {

  @Operation(summary = "Register new user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "New user should be registered.",
            content =
                @Content(
                    schema = @Schema(implementation = UserResponseDto.class),
                    mediaType = "application/json")),
        @ApiResponse(
            responseCode = "400",
            ref = "#/components/responses/methodArgumentNotValidResponse"),
        @ApiResponse(
            responseCode = "500",
            description = "Failed to send Email",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = "application/json")),
        @ApiResponse(
            responseCode = "409",
            description = "There already is an duplicate value either for email or phone number.",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = "application/json"))
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
                    mediaType = "application/json")),
        @ApiResponse(
            responseCode = "404",
            description = "Email confirmation token is not found",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = "application/json"))
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
                    mediaType = "application/json")),
        @ApiResponse(
            responseCode = "400",
            ref = "#/components/responses/methodArgumentNotValidResponse"),
        @ApiResponse(
            responseCode = "401",
            description = "Bad credentials",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = "application/json"))
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
                    mediaType = "application/json")),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid refresh token was sent",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = "application/json")),
        @ApiResponse(
            responseCode = "401",
            description = "Expired refresh token was sent",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = "application/json")),
        @ApiResponse(
            responseCode = "404",
            description = "Credentials not found",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = "application/json"))
      })
  ResponseEntity<RefreshTokenResponseDto> refreshToken(
      RefreshTokenRequestDto refreshTokenRequestDto);

  @Operation(
      summary = "Fetch users sorted paged and sorted",
      security = @SecurityRequirement(name = "api-security"))
  @RequestBody(
      content = @Content(schema = @Schema(implementation = FetchRequestDto.class)),
      description =
          "Each json property has it's default value, so the endpoint allows request with at least one property")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Fetched users from DB",
            content =
                @Content(
                    schema = @Schema(implementation = FetchResponseDto.class),
                    mediaType = "application/json")),
      })
  ResponseEntity<FetchResponseDto> fetchUsers(FetchRequestDto viewRequest);

  @Operation(
      summary = "Change user password",
      security = @SecurityRequirement(name = "api-security"))
  @RequestBody(
      content =
          @Content(
              schema = @Schema(implementation = ChangePasswordRequestDto.class),
              mediaType = "application/json"))
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "User password was successfully changed"),
        @ApiResponse(
            responseCode = "400",
            ref = "#/components/responses/methodArgumentNotValidResponse"),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = "application/json")),
        @ApiResponse(
            responseCode = "409",
            description = "Provided The Same Password In Change Password Request",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = "application/json")),
        @ApiResponse(
            responseCode = "400",
            description = "Provided Wrong Password In Change Password Request",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = "application/json"))
      })
  ResponseEntity<Void> changePassword(ChangePasswordRequestDto request, AppUserDetails userDetails);

  @Operation(summary = "Get user profile", security = @SecurityRequirement(name = "api-security"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Fetched a user from DB",
            content =
                @Content(
                    schema = @Schema(implementation = UserResponseDto.class),
                    mediaType = "application/json")),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = "application/json"))
      })
  ResponseEntity<UserResponseDto> getUser(UUID userId);

  @Operation(summary = "Request to receive an email to restore the password")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Email was sent to user"),
        @ApiResponse(
            responseCode = "500",
            description = "Failed to send Email",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = "application/json")),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content =
                @Content(
                    schema = @Schema(implementation = ApiError.class),
                    mediaType = "application/json"))
      })
  ResponseEntity<Void> forgotPassword(String email, Locale locale);

  @Operation(summary = "Restore forgotten password")
  @RequestBody(
      content =
          @Content(
              schema = @Schema(implementation = RestorePasswordRequestDto.class),
              mediaType = "application/json"))
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
                    mediaType = "application/json"))
      })
  ResponseEntity<Void> restorePassword(RestorePasswordRequestDto restorePasswordRequestDto);
}

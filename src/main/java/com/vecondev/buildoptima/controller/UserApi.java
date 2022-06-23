package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.dto.request.*;
import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.UserResponseDto;
import com.vecondev.buildoptima.dto.response.FetchResponse;
import com.vecondev.buildoptima.exception.ApiError;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
        @ApiResponse(
            responseCode = "400",
            ref = "#/components/responses/methodArgumentNotValidResponse"),
        @ApiResponse(
            responseCode = "400",
            description = "Failed to send Email",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "409",
            description = "There already is an duplicate value either for email or phone number.",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<UserResponseDto> register(
      UserRegistrationRequestDto userRegistrationRequestDto, Locale locale);

  @Operation(summary = "Activate email")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User account activated by email confirmation",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Email confirmation token is not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<UserResponseDto> activate(String token);

  @Operation(summary = "User sign in")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User signed in",
            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
        @ApiResponse(
            responseCode = "400",
            ref = "#/components/responses/methodArgumentNotValidResponse"),
        @ApiResponse(
            responseCode = "401",
            description = "Bad credentials",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<AuthResponseDto> login(AuthRequestDto authRequestDto);

  @Operation(summary = "Refresh test user token")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "New access token was created for user",
            content = @Content(schema = @Schema(implementation = RefreshTokenResponseDto.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid refresh token was sent",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Expired refresh token was sent",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Credentials not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<RefreshTokenResponseDto> refreshToken(
      RefreshTokenRequestDto refreshTokenRequestDto);

  @Operation(summary = "Fetch users sorted paged and sorted")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Fetched users from DB",
            content = @Content(schema = @Schema(implementation = FetchResponse.class))),
      })
  ResponseEntity<FetchResponse> fetchUsers(FetchRequest viewRequest);

  @Operation(summary = "Change user password")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "User password was successfully changed"),
        @ApiResponse(
            responseCode = "400",
            ref = "#/components/responses/methodArgumentNotValidResponse"),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Provided The Same Password In Change Password Request",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Provided Wrong Password In Change Password Request",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<Void> changePassword(ChangePasswordRequest request, AppUserDetails userDetails);

  @Operation(summary = "Get user profile")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Fetched a user from DB"),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  ResponseEntity<UserResponseDto> getUser(UUID userId);
}

package com.vecondev.buildoptima.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Refresh Token Response DTO")
public class RefreshTokenResponseDto {

  private String accessToken;
  private String refreshToken;
}

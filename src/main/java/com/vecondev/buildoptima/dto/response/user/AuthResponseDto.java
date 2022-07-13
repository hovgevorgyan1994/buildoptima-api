package com.vecondev.buildoptima.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Authentication Response DTO")
public class AuthResponseDto {

  private String accessToken;

  private String refreshTokenId;
}

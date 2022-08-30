package com.vecondev.buildoptima.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
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

  private UUID userId;

  private Integer imageVersion;

  private String accessToken;

  private String refreshToken;
}

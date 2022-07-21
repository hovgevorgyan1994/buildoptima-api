package com.vecondev.buildoptima.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Refresh Token Request DTO")
public class RefreshTokenRequestDto {

  @NotBlank private String refreshToken;
}

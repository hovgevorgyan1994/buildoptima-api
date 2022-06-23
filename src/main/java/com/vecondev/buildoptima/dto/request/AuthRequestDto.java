package com.vecondev.buildoptima.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Authentication Request DTO")
public class AuthRequestDto {

  @NotBlank private String username;

  @NotBlank private String password;
}

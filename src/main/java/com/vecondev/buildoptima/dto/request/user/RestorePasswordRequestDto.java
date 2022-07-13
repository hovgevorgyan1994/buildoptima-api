package com.vecondev.buildoptima.dto.request.user;

import com.vecondev.buildoptima.validation.constraint.Password;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Restore Password Request DTO")
public class RestorePasswordRequestDto {

  @NotNull private String confirmationToken;

  @NotNull
  @Password
  @Schema(
          description =
                  "User's password: should have valid password format, at least one uppercase character, "
                          + "one lowercase character, one digit, one special symbol and no whitespaces!",
          example = "Example1234.",
          minLength = 8,
          maxLength = 32)
  private String newPassword;
}

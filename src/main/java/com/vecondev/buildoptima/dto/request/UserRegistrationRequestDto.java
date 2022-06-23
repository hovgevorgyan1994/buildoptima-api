package com.vecondev.buildoptima.dto.request;

import com.vecondev.buildoptima.validation.constraint.Name;
import com.vecondev.buildoptima.validation.constraint.Password;
import com.vecondev.buildoptima.validation.constraint.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
@Schema(name = "User Registration Request DTO")
public class UserRegistrationRequestDto {

  @Name
  @Schema(
      description = "User's first name.",
      example = "John",
      required = true,
      minLength = 2,
      maxLength = 20,
      pattern = "^[A-Za-z]*$")
  private String firstName;

  @Name
  @Schema(
      description = "User's last name.",
      example = "Smith",
      required = true,
      minLength = 2,
      maxLength = 20,
      pattern = "^[A-Za-z]*$")
  private String lastName;

  @NotNull
  @Phone
  @Schema(
      description = "User's phone number.",
      example = "+37477123456",
      pattern = "^[+]{1}[0-9]{10,14}$")
  private String phone;

  @NotNull
  @Email
  @Schema(
      description = "User's email: should have valid email format",
      example = "example@gmail.com")
  private String email;

  @NotNull
  @Password
  @Schema(
      description =
          "User's password: should have valid password format, at least one uppercase character, "
              + "one lowercase character, one digit, one special symbol and no whitespaces!",
      example = "Example1234.",
      minLength = 8,
      maxLength = 32)
  private String password;
}

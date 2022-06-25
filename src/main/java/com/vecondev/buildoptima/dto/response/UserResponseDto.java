package com.vecondev.buildoptima.dto.response;

import com.vecondev.buildoptima.model.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@Schema(name = "User Response DTO")
public class UserResponseDto {

  @Schema(example = "2635b586-d0d7-4a2d-b4b5-c98377a02322")
  private UUID id;

  @Schema(example = "John")
  private String firstName;

  @Schema(example = "Smith")
  private String lastName;

  @Schema(example = "+37477123456")
  private String phone;

  @Schema(example = "example@mail.ru")
  private String email;

  @Schema(example = "CLIENT")
  private Role role;

  @Schema(example = "2022-06-24 13:29:00.887950")
  private Instant creationDate;

  @Schema(example = "2022-06-24 13:29:00.887950")
  private Instant updateDate;
}

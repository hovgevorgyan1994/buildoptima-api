package com.vecondev.buildoptima.dto.user;

import com.vecondev.buildoptima.model.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@Schema(name = "User registration response DTO")
public class UserRegistrationResponseDto {

    private UUID id;

    private String firstName;
    private String lastName;

    private String phone;
    private String email;
    private Role role;

    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
}

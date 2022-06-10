package com.vecondev.buildoptima.model;


import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_token")
public class RefreshToken extends AbstractEntity{

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}

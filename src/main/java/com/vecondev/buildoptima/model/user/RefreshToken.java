package com.vecondev.buildoptima.model.user;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;

  @Column(name = "refresh_token")
  private String refreshToken;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  public RefreshToken(String refreshToken, UUID userId, LocalDateTime expiresAt) {
    this.refreshToken = refreshToken;
    this.userId = userId;
    this.expiresAt = expiresAt;
  }
}

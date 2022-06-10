package com.vecondev.buildoptima.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends AbstractEntity {

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "phone")
  private String phone;

  @Column(name = "email")
  private String email;

  @Column(name = "password")
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "role")
  private com.vecondev.buildoptima.model.user.Role role;

  @CreationTimestamp
  @Column(name = "creation_date")
  private LocalDateTime creationDate;

  @UpdateTimestamp
  @Column(name = "update_date")
  private LocalDateTime updateDate;

  @Column(name = "enabled")
  private boolean enabled;
}

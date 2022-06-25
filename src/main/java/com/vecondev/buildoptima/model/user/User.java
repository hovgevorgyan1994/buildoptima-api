package com.vecondev.buildoptima.model.user;

import com.vecondev.buildoptima.model.AbstractEntity;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
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
  private Role role;

  @CreationTimestamp
  @Column(name = "creation_date")
  private Instant creationDate;

  @UpdateTimestamp
  @Column(name = "update_date")
  private Instant updateDate;

  @Column(name = "enabled")
  @ColumnDefault("false")
  private Boolean enabled;
}

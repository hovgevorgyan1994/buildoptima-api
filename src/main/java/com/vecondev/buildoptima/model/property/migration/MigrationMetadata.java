package com.vecondev.buildoptima.model.property.migration;

import com.vecondev.buildoptima.dto.property.AddressDto;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bo_migration_metadata")
@TypeDef(name = "json", typeClass = JsonBinaryType.class)
public class MigrationMetadata implements Serializable {

  @Serial private static final long serialVersionUID = -8940950886348371170L;

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "migration_history_id")
  private MigrationHistory migrationHistory;

  @Column(name = "ain")
  private String ain;

  @Type(type = "json")
  @Column(name = "addresses", columnDefinition = "json")
  private List<AddressDto> addresses;

  @Column(name = "synced_at")
  private Instant syncedAt;

  @Column(name = "failed_at")
  private Instant failedAt;

  @Column(name = "failed_reason")
  private String failedReason;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    MigrationMetadata that = (MigrationMetadata) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}

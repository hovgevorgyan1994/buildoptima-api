package com.vecondev.buildoptima.model.property;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static javax.persistence.CascadeType.ALL;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bo_property")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Property implements Serializable {

  @Serial private static final long serialVersionUID = -1708400421457836238L;

  @Id
  @Column(name = "ain")
  private String ain;

  @Column(name = "municipality")
  private String municipality;

  @OneToMany(mappedBy = "property", cascade = ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JsonBackReference
  private List<Address> addresses;

  @Type(type = "jsonb")
  @Column(name = "locations", columnDefinition = "jsonb")
  private Locations locations;

  @Type(type = "jsonb")
  @Column(name = "details", columnDefinition = "jsonb")
  private Details details;

  @Type(type = "jsonb")
  @Column(name = "hazards", columnDefinition = "jsonb")
  private Hazards hazards;

  @Type(type = "jsonb")
  @Column(name = "zoning_details", columnDefinition = "jsonb")
  private ZoningDetails zoningDetails;

  @Version
  @Column(name = "version", columnDefinition = "integer DEFAULT 0")
  private Integer version;

  @CreationTimestamp
  @Column(name = "created_at")
  protected Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  protected Instant updatedAt;

  public void addAddresses(List<Address> addresses) {
    this.addresses = new ArrayList<>();
    this.addresses.addAll(addresses);
    addresses.forEach(address -> address.setProperty(this));
  }

  public void removeAddresses(List<Address> addresses) {
    this.addresses.removeAll(addresses);
    addresses.forEach(address -> address.setProperty(null));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Property property = (Property) o;
    return Objects.equals(ain, property.ain)
        && Objects.equals(municipality, property.municipality)
        && Objects.equals(addresses, property.addresses)
        && Objects.equals(locations, property.locations)
        && Objects.equals(details, property.details)
        && Objects.equals(hazards, property.hazards)
        && Objects.equals(zoningDetails, property.zoningDetails)
        && Objects.equals(version, property.version)
        && Objects.equals(createdAt, property.createdAt)
        && Objects.equals(updatedAt, property.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        ain,
        municipality,
        //        addresses,
        locations,
        details,
        hazards,
        zoningDetails,
        version,
        createdAt,
        updatedAt);
  }
}

package com.vecondev.buildoptima.model.faq;

import com.vecondev.buildoptima.model.AbstractEntity;
import com.vecondev.buildoptima.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@Entity
@Table(name = "faq_category")
public class FaqCategory extends AbstractEntity {

  @Column(name = "name")
  private String name;

  @ManyToOne private User createdBy;

  @ManyToOne private User updatedBy;

  public FaqCategory(
      String name, User createdBy, User updatedBy, Instant createdAt, Instant updatedAt) {
    super(createdAt, updatedAt);
    this.name = name;
    this.createdBy = createdBy;
    this.updatedBy = updatedBy;
  }
}

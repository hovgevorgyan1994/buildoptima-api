package com.vecondev.buildoptima.model.faq;

import com.vecondev.buildoptima.model.AbstractEntity;
import com.vecondev.buildoptima.model.Status;
import com.vecondev.buildoptima.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@Entity
@Table(name = "faq_question")
public class FaqQuestion extends AbstractEntity {

  @Column(name = "question")
  private String question;

  @Column(name = "answer")
  private String answer;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private Status status;

  @ManyToOne private FaqCategory category;

  @ManyToOne
  private User updatedBy;

  @CreationTimestamp
  @Column(name = "created_at")
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}

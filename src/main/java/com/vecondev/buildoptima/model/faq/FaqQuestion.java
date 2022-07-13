package com.vecondev.buildoptima.model.faq;

import com.vecondev.buildoptima.model.AbstractEntity;
import com.vecondev.buildoptima.model.Status;
import com.vecondev.buildoptima.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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

  @ManyToOne private User createdBy;

  @ManyToOne private User updatedBy;
}

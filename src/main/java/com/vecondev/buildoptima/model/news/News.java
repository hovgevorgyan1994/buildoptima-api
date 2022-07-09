package com.vecondev.buildoptima.model.news;

import com.vecondev.buildoptima.model.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "news")
public class News extends AbstractEntity {

  @Column(name = "title")
  private String title;

  @Column(name = "summary")
  private String summary;

  @ElementCollection(targetClass = String.class)
  @CollectionTable(name = "news_keywords", joinColumns = @JoinColumn(name = "news_id"))
  @Column(name = "keyword")
  private Set<String> keywords;

  @Column(name = "published_at")
  @CreationTimestamp
  private Instant publishedAt;

  @Column(name = "modified_by")
  private String modifiedBy;
}

package com.vecondev.buildoptima.repository.news;

import com.vecondev.buildoptima.model.Status;
import com.vecondev.buildoptima.model.news.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, UUID>, JpaSpecificationExecutor<News> {

  News findTopByOrderByUpdatedAtDesc();

  long countByStatus(Status status);
}

package io.github.sakujj.nms.repository;

import io.github.sakujj.nms.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A JPA repository for {@link News} entity
 */
@Repository
public interface NewsRepository extends JpaRepository<News, UUID> {

    Page<News> findByTitleContaining(String containedInTitle, Pageable pageable);

    Page<News> findByTitleContainingAndUsernameContaining(String containedInTitle, String containedInUsername, Pageable pageable);

    Page<News> findByUsernameContaining(String containedInUsername, Pageable pageable);
}

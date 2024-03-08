package io.github.sakujj.nms.repository;

import io.github.sakujj.nms.entity.NewsId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NewsIdRepository extends JpaRepository<NewsId, UUID> {
}

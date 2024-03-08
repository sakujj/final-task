package io.github.sakujj.nms.service;

import io.github.sakujj.nms.entity.NewsId;

import java.util.Optional;
import java.util.UUID;

public interface NewsIdService {

    void deleteById(UUID id);

    void create(UUID id, UUID authorId);

    Optional<NewsId> findById(UUID id);
}

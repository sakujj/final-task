package io.github.sakujj.nms.service;

import io.github.sakujj.nms.dto.NewsRequest;
import io.github.sakujj.nms.dto.NewsResponse;
import io.github.sakujj.nms.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface NewsService {

    Optional<NewsResponse> findById(UUID id);

    Page<NewsResponse> findAll(int pageNumber, int pageSize);

    Page<NewsResponse> findByTitleContaining(String content, int pageNumber, int pageSize);

    Page<NewsResponse> findByUsernameContaining(String content, int pageNumber, int pageSize);

    NewsResponse save(NewsRequest newsRequest, UUID authorId, String username);

    void deleteById(UUID id);

    boolean updateById(UUID id, NewsRequest newsRequest);
}

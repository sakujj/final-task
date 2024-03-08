package io.github.sakujj.nms.service;

import io.github.sakujj.cache.aop.CacheableCreate;
import io.github.sakujj.cache.aop.CacheableDeleteByUUID;
import io.github.sakujj.cache.aop.CacheableFindByUUID;
import io.github.sakujj.cache.aop.CacheableUpdateByUUID;
import io.github.sakujj.nms.dto.NewsRequest;
import io.github.sakujj.nms.dto.NewsResponse;
import io.github.sakujj.nms.entity.News;
import io.github.sakujj.nms.mapper.NewsMapper;
import io.github.sakujj.nms.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;

    @Override
    @CacheableFindByUUID
    public Optional<NewsResponse> findById(UUID id) {
        return newsRepository.findById(id)
                .map(newsMapper::toResponse);
    }

    @Override
    public Page<NewsResponse> findAll(int pageNumber, int pageSize) {

        Sort timeSort = Sort.by(Sort.Direction.DESC, News.Fields.CREATION_TIME);
        Pageable pageableSorted = PageRequest.of(pageNumber, pageSize, timeSort);

        Page<News> pageFound = newsRepository.findAll(pageableSorted);

        return pageFound.map(newsMapper::toResponse);
    }

    @Override
    public Page<NewsResponse> findByTitleContaining(String containedInTitle, int pageNumber, int pageSize) {

        Sort timeSort = Sort.by(Sort.Direction.DESC, News.Fields.CREATION_TIME);
        Pageable pageableSorted = PageRequest.of(pageNumber, pageSize, timeSort);

        Page<News> pageFound = newsRepository.findByTitleContaining(containedInTitle, pageableSorted);

        return pageFound.map(newsMapper::toResponse);
    }

    @Override
    public Page<NewsResponse> findByUsernameContaining(String containedInUsername, int pageNumber, int pageSize) {

        Sort timeSort = Sort.by(Sort.Direction.DESC, News.Fields.CREATION_TIME);
        Pageable pageableSorted = PageRequest.of(pageNumber, pageSize, timeSort);

        Page<News> pageFound = newsRepository.findByUsernameContaining(containedInUsername, pageableSorted);

        return pageFound.map(newsMapper::toResponse);
    }

    @Override
    public Page<NewsResponse> findByTitleContainingAndUsernameContaining(String containedInTitle,
                                                                         String containedInUsername,
                                                                         int pageNumber,
                                                                         int pageSize) {

        Sort timeSort = Sort.by(Sort.Direction.DESC, News.Fields.CREATION_TIME);
        Pageable pageableSorted = PageRequest.of(pageNumber, pageSize, timeSort);

        Page<News> pageFound = newsRepository.findByTitleContainingAndUsernameContaining(
                containedInTitle,
                containedInUsername,
                pageableSorted);

        return pageFound.map(newsMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheableCreate
    public NewsResponse create(NewsRequest newsRequest, UUID authorId, String username) {

        News newsToSave = newsMapper.fromRequest(newsRequest);

        LocalDateTime currentTime = LocalDateTime.now();
        UUID uuidGenerated = UUID.randomUUID();

        newsToSave.setId(uuidGenerated);
        newsToSave.setCreationTime(currentTime);
        newsToSave.setUpdateTime(currentTime);
        newsToSave.setAuthorId(authorId);
        newsToSave.setUsername(username);

        News newsSaved = newsRepository.save(newsToSave);

        return newsMapper.toResponse(newsSaved);
    }

    @Override
    @Transactional
    @CacheableDeleteByUUID
    public void deleteById(UUID id) {
        newsRepository.deleteById(id);
    }

    @CacheableUpdateByUUID
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Optional<NewsResponse> update(UUID id, NewsRequest newsRequest) {

        Optional<News> newsOptional = newsRepository.findById(id);
        if (newsOptional.isEmpty()) {

            return Optional.empty();
        }

        News newsToUpdate = newsOptional.get();

        String textUpdated = newsRequest.getText();
        String titleUpdated = newsRequest.getTitle();
        LocalDateTime updateTime = LocalDateTime.now();

        newsToUpdate.setText(textUpdated);
        newsToUpdate.setTitle(titleUpdated);
        newsToUpdate.setUpdateTime(updateTime);

        News newsUpdated = newsRepository.save(newsToUpdate);

        NewsResponse newsResponse = newsMapper.toResponse(newsUpdated);

        return Optional.of(newsResponse);
    }
}

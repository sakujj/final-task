package io.github.sakujj.nms.service;

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
import org.springframework.stereotype.Service;
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
    public Optional<NewsResponse> findById(UUID id) {

        return newsRepository.findById(id)
                .map(newsMapper::toResponse);
    }

    @Override
    public Page<NewsResponse> findAll(int pageNumber, int pageSize) {

        Sort timeSort = Sort.by(Sort.Direction.DESC, News.CREATION_TIME_COLUMN_NAME);
        Pageable pageableSorted = PageRequest.of(pageNumber, pageSize, timeSort);

        Page<News> pageFound = newsRepository.findAll(pageableSorted);

        return pageFound.map(newsMapper::toResponse);
    }

    @Override
    public Page<NewsResponse> findByTitleContaining(String content, int pageNumber, int pageSize) {

        Sort timeSort = Sort.by(Sort.Direction.DESC, News.CREATION_TIME_COLUMN_NAME);
        Pageable pageableSorted = PageRequest.of(pageNumber, pageSize, timeSort);

        Page<News> pageFound = newsRepository.findByTitleContaining(content, pageableSorted);

        return pageFound.map(newsMapper::toResponse);
    }

    @Override
    public Page<NewsResponse> findByUsernameContaining(String content, int pageNumber, int pageSize) {

        Sort timeSort = Sort.by(Sort.Direction.DESC, News.CREATION_TIME_COLUMN_NAME);
        Pageable pageableSorted = PageRequest.of(pageNumber, pageSize, timeSort);

        Page<News> pageFound = newsRepository.findByUsernameContaining(content, pageableSorted);

        return pageFound.map(newsMapper::toResponse);
    }

    @Override
    @Transactional
    public NewsResponse save(NewsRequest newsRequest, UUID authorId, String username) {

        News newsToSave = newsMapper.fromRequest(newsRequest);

        LocalDateTime currentTime = LocalDateTime.now();

        newsToSave.setCreationTime(currentTime);
        newsToSave.setUpdateTime(currentTime);
        newsToSave.setAuthorId(authorId);
        newsToSave.setUsername(username);

        News newsSaved = newsRepository.save(newsToSave);

        return newsMapper.toResponse(newsSaved);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {

        newsRepository.deleteById(id);
    }

    @Override
    @Transactional
    public boolean updateById(UUID id, NewsRequest newsRequest) {

        News newsToUpdate = newsMapper.fromRequest(newsRequest);

        LocalDateTime updateTime = LocalDateTime.now();
        String titleUpdated = newsToUpdate.getTitle();
        String textUpdated = newsToUpdate.getText();

        return newsRepository.updateById(id, textUpdated, titleUpdated, updateTime) > 0;
    }
}

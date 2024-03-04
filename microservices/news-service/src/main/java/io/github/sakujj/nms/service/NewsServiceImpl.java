package io.github.sakujj.nms.service;

import io.github.sakujj.nms.constant.RoleConstants;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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
    public NewsResponse createWithAssignedId(NewsRequest newsRequest, UUID assignedId, UUID authorId, String username) {

        News newsToSave = newsMapper.fromRequest(newsRequest);

        LocalDateTime currentTime = LocalDateTime.now();

        newsToSave.setId(assignedId);
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
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ResponseEntity<NewsResponse> replace(UUID id,
                                                NewsRequest newsRequest,
                                                Collection<GrantedAuthority> authoritiesOfAuthenticatedUser,
                                                UUID idOfAuthenticatedUser,
                                                String usernameOfAuthenticatedUser) {

        List<String> stringAuthorities = authoritiesOfAuthenticatedUser.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        boolean noAdminAuthority = !stringAuthorities.contains(RoleConstants.ADMIN);
        boolean noJournalistAuthority = !stringAuthorities.contains(RoleConstants.JOURNALIST);

        if (noAdminAuthority && noJournalistAuthority) {

            throw new AccessDeniedException("User with id %s is unauthorized to change news."
                    .formatted(idOfAuthenticatedUser));
        }

        Optional<News> newsOptional = newsRepository.findById(id);
        if (newsOptional.isEmpty()) {

            NewsResponse createdNews = this.createWithAssignedId(
                    newsRequest,
                    id,
                    idOfAuthenticatedUser,
                    usernameOfAuthenticatedUser);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createdNews);
        }

        News news = newsOptional.get();
        UUID idOfNewsAuthor = news.getAuthorId();

        if (noAdminAuthority && idOfAuthenticatedUser != idOfNewsAuthor) {

            throw new AccessDeniedException("User with id %s is unauthorized to change news with id %s of the author %s."
                            .formatted(idOfAuthenticatedUser, idOfNewsAuthor, idOfNewsAuthor));
        }

        String textUpdated = newsRequest.getText();
        String titleUpdated = newsRequest.getTitle();
        LocalDateTime updateTime = LocalDateTime.now();

        news.setText(textUpdated);
        news.setTitle(titleUpdated);
        news.setUpdateTime(updateTime);

        News newsReplaced = newsRepository.save(news);

        NewsResponse newsResponse = newsMapper.toResponse(newsReplaced);

        return ResponseEntity.ok(newsResponse);
    }
}

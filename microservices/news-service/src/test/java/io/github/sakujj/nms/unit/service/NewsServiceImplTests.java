package io.github.sakujj.nms.unit.service;

import io.github.sakujj.nms.constant.RoleConstants;
import io.github.sakujj.nms.dto.NewsRequest;
import io.github.sakujj.nms.dto.NewsResponse;
import io.github.sakujj.nms.entity.News;
import io.github.sakujj.nms.mapper.NewsMapper;
import io.github.sakujj.nms.repository.NewsRepository;
import io.github.sakujj.nms.service.NewsServiceImpl;
import io.github.sakujj.nms.util.NewsTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NewsServiceImplTests {

    private NewsMapper newsMapper;

    private NewsRepository newsRepository;

    private NewsServiceImpl newsServiceImpl;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Captor
    private ArgumentCaptor<News> newsCaptor;

    @BeforeEach
    void initMocks() {
        newsRepository = Mockito.mock(NewsRepository.class);
        newsMapper = Mockito.mock(NewsMapper.class);

        newsServiceImpl = Mockito.spy(new NewsServiceImpl(newsRepository, newsMapper));
    }

    @Nested
    @DisplayName("findById (UUID)")
    class findById {
        @Test
        void shouldFindById() {

            // given
            NewsTestBuilder anArticle = NewsTestBuilder.anArticle();
            NewsResponse expected = anArticle.buildResponse();
            News news = anArticle.build();
            UUID idToFindBy = news.getId();

            when(newsMapper.toResponse(news))
                    .thenReturn(expected);
            when(newsRepository.findById(idToFindBy))
                    .thenReturn(Optional.of(news));

            // when
            Optional<NewsResponse> actual = newsServiceImpl.findById(idToFindBy);

            // then
            assertThat(actual).isPresent();
            assertThat(actual.get()).isEqualTo(expected);
        }

        @Test
        void shouldReturnOptionalEmpty_whenNotFoundById() {

            // given
            UUID idToFindBy = NewsTestBuilder.anArticle().getId();

            when(newsRepository.findById(idToFindBy))
                    .thenReturn(Optional.empty());

            // when
            Optional<NewsResponse> actual = newsServiceImpl.findById(idToFindBy);

            // then
            assertThat(actual).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll (int, int)")
    class findAll {

        @Test
        void shouldFindAllSortedDescByCreationTime() {

            // given
            List<News> news = NewsTestBuilder.getTestList().stream()
                    .map(NewsTestBuilder::build)
                    .toList();
            List<NewsResponse> newsResponses = NewsTestBuilder.getTestList().stream()
                    .map(NewsTestBuilder::buildResponse)
                    .toList();
            Page<NewsResponse> expected = new PageImpl<>(newsResponses);

            when(newsRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(news));
            IntStream.range(0, news.size())
                    .forEach(i -> {
                        News newsToMap = news.get(i);
                        NewsResponse mappedResponse = newsResponses.get(i);

                        when(newsMapper.toResponse(newsToMap))
                                .thenReturn(mappedResponse);
                    });

            int pageNumber = 3;
            int pageSize = 15;
            String propertyToSortBy = News.Fields.CREATION_TIME;

            // when
            Page<NewsResponse> actual = newsServiceImpl.findAll(pageNumber, pageSize);

            // then
            verify(newsRepository).findAll(pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();

            assertThat(pageable.getPageNumber()).isEqualTo(pageNumber);
            assertThat(pageable.getPageSize()).isEqualTo(pageSize);

            Sort.Order order = pageable.getSort().getOrderFor(propertyToSortBy);
            assertThat(order).isNotNull();
            assertThat(order.getDirection().isDescending()).isTrue();

            assertThat(actual).isEqualTo(expected);
        }

    }

    @Nested
    @DisplayName("findByTitleContaining (String, int, int)")
    class findByTitleContaining {

        @Test
        void shouldFindByTitleContainingSortedDescByCreationTime() {

            // given
            String containedInTitle = "some info";

            List<News> news = NewsTestBuilder.getTestList().stream()
                    .map(NewsTestBuilder::build)
                    .toList();
            List<NewsResponse> newsResponses = NewsTestBuilder.getTestList().stream()
                    .map(NewsTestBuilder::buildResponse)
                    .toList();
            Page<NewsResponse> expected = new PageImpl<>(newsResponses);

            when(newsRepository.findByTitleContaining(eq(containedInTitle), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(news));
            IntStream.range(0, news.size())
                    .forEach(i -> {
                        News newsToMap = news.get(i);
                        NewsResponse mappedResponse = newsResponses.get(i);

                        when(newsMapper.toResponse(newsToMap))
                                .thenReturn(mappedResponse);
                    });

            int pageNumber = 3;
            int pageSize = 15;
            String propertyToSortBy = News.Fields.CREATION_TIME;

            // when
            Page<NewsResponse> actual = newsServiceImpl.findByTitleContaining(containedInTitle, pageNumber, pageSize);

            // then
            verify(newsRepository).findByTitleContaining(eq(containedInTitle), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();

            assertThat(pageable.getPageNumber()).isEqualTo(pageNumber);
            assertThat(pageable.getPageSize()).isEqualTo(pageSize);

            Sort.Order order = pageable.getSort().getOrderFor(propertyToSortBy);
            assertThat(order).isNotNull();
            assertThat(order.getDirection().isDescending()).isTrue();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("findByUsernameContaining (String, int, int)")
    class findByUsernameContaining {

        @Test
        void shouldFindByUsernameContainingSortedDescByCreationTime() {

            // given
            String containedInUsername = "some info";

            List<News> news = NewsTestBuilder.getTestList().stream()
                    .map(NewsTestBuilder::build)
                    .toList();
            List<NewsResponse> newsResponses = NewsTestBuilder.getTestList().stream()
                    .map(NewsTestBuilder::buildResponse)
                    .toList();
            Page<NewsResponse> expected = new PageImpl<>(newsResponses);

            when(newsRepository.findByUsernameContaining(eq(containedInUsername), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(news));
            IntStream.range(0, news.size())
                    .forEach(i -> {
                        News newsToMap = news.get(i);
                        NewsResponse mappedResponse = newsResponses.get(i);

                        when(newsMapper.toResponse(newsToMap))
                                .thenReturn(mappedResponse);
                    });

            int pageNumber = 3;
            int pageSize = 15;
            String propertyToSortBy = News.Fields.CREATION_TIME;

            // when
            Page<NewsResponse> actual = newsServiceImpl.findByUsernameContaining(containedInUsername, pageNumber, pageSize);

            // then
            verify(newsRepository).findByUsernameContaining(eq(containedInUsername), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();

            assertThat(pageable.getPageNumber()).isEqualTo(pageNumber);
            assertThat(pageable.getPageSize()).isEqualTo(pageSize);

            Sort.Order order = pageable.getSort().getOrderFor(propertyToSortBy);
            assertThat(order).isNotNull();
            assertThat(order.getDirection().isDescending()).isTrue();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("findByTitleContainingAndUsernameContaining (String, String, int, int)")
    class findByTitleContainingAndUsernameContaining {

        @Test
        void shouldFindByTitleContainingAndUsernameContainingSortedDescByCreationTime() {

            // given
            String containedInUsername = "some info in username";
            String containedInTitle = "some info in title";

            List<News> news = NewsTestBuilder.getTestList().stream()
                    .map(NewsTestBuilder::build)
                    .toList();
            List<NewsResponse> newsResponses = NewsTestBuilder.getTestList().stream()
                    .map(NewsTestBuilder::buildResponse)
                    .toList();
            Page<NewsResponse> expected = new PageImpl<>(newsResponses);

            when(newsRepository.findByTitleContainingAndUsernameContaining(
                    eq(containedInTitle),
                    eq(containedInUsername),
                    any(Pageable.class))
            ).thenReturn(new PageImpl<>(news));

            IntStream.range(0, news.size())
                    .forEach(i -> {
                        News newsToMap = news.get(i);
                        NewsResponse mappedResponse = newsResponses.get(i);

                        when(newsMapper.toResponse(newsToMap))
                                .thenReturn(mappedResponse);
                    });

            int pageNumber = 3;
            int pageSize = 15;
            String propertyToSortBy = News.Fields.CREATION_TIME;

            // when
            Page<NewsResponse> actual = newsServiceImpl.findByTitleContainingAndUsernameContaining(
                    containedInTitle,
                    containedInUsername,
                    pageNumber, pageSize);

            // then
            verify(newsRepository).findByTitleContainingAndUsernameContaining(
                    eq(containedInTitle),
                    eq(containedInUsername),
                    pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();

            assertThat(pageable.getPageNumber()).isEqualTo(pageNumber);
            assertThat(pageable.getPageSize()).isEqualTo(pageSize);

            Sort.Order order = pageable.getSort().getOrderFor(propertyToSortBy);
            assertThat(order).isNotNull();
            assertThat(order.getDirection().isDescending()).isTrue();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("deleteById (UUID)")
    class deleteById {

        @Test
        void shouldDeleteById() {

            // given
            UUID id = NewsTestBuilder.anArticle().getId();

            // when
            newsServiceImpl.deleteById(id);

            // then
            verify(newsRepository).deleteById(id);
        }
    }

    @Nested
    @DisplayName("create (NewsRequest, UUID, String)")
    class create {

        @Test
        void shouldCreate() {

            // given
            NewsTestBuilder anArticle = NewsTestBuilder.anArticle();

            NewsRequest newsRequestToCreate = anArticle.buildRequest();
            UUID expectedAuthorId = anArticle.getAuthorId();
            String expectedUsername = anArticle.getUsername();

            News mappedNewsFromRequest = News.builder()
                    .title(newsRequestToCreate.getTitle())
                    .text(newsRequestToCreate.getText())
                    .build();

            when(newsMapper.fromRequest(newsRequestToCreate))
                    .thenReturn(mappedNewsFromRequest);

            News expectedFromRepo = NewsTestBuilder.anArticle().build();
            when(newsRepository.save(any(News.class)))
                    .thenReturn(expectedFromRepo);

            NewsResponse expected = NewsTestBuilder.anArticle().buildResponse();
            when(newsMapper.toResponse(same(expectedFromRepo)))
                    .thenReturn(expected);

            LocalDateTime timeBeforeTest = LocalDateTime.now();

            // when
            NewsResponse actual = newsServiceImpl.create(newsRequestToCreate, expectedAuthorId, expectedUsername);

            // then
            verify(newsRepository).save(newsCaptor.capture());
            News newsCaptured = newsCaptor.getValue();

            assertThat(newsCaptured.getCreationTime()).isAfter(timeBeforeTest);
            assertThat(newsCaptured.getUpdateTime()).isAfter(timeBeforeTest);
            assertThat(newsCaptured.getId()).isNotNull();

            assertThat(newsCaptured.getUsername()).isEqualTo(expectedUsername);
            assertThat(newsCaptured.getAuthorId()).isEqualTo(expectedAuthorId);
            assertThat(newsCaptured.getText()).isEqualTo(newsRequestToCreate.getText());
            assertThat(newsCaptured.getTitle()).isEqualTo(newsRequestToCreate.getTitle());

            assertThat(actual).isSameAs(expected);
        }
    }

    @Nested
    @DisplayName("update (UUID, NewsRequest)")
    class update {

        @Test
        void shouldReturnEmptyOptional_whenNotFound_onUpdate() {

            // given
            NewsTestBuilder anArticle = NewsTestBuilder.anArticle();

            NewsRequest newsRequest = anArticle.buildRequest();
            UUID idOfNewsToReplace = anArticle.getId();

            when(newsRepository.findById(idOfNewsToReplace))
                    .thenReturn(Optional.empty());

            // when
            Optional<NewsResponse> updatedOptional = newsServiceImpl.update(idOfNewsToReplace, newsRequest);

            // then
            assertThat(updatedOptional).isEmpty();
        }

        @Test
        void shouldUpdate() {

            // given
            NewsRequest newsRequest = NewsTestBuilder.anArticle()
                    .withTitle("some new title cvxvgf")
                    .withText("some new text vcvxcfgx")
                    .buildRequest();

            UUID idOfNewsToReplace = NewsTestBuilder.anArticle().getId();

            News newsFromFind = NewsTestBuilder.anArticle().build();
            NewsResponse newsResponse = NewsTestBuilder.anArticle().buildResponse();

            News newsFromSave = NewsTestBuilder.anArticle().build();

            when(newsRepository.findById(idOfNewsToReplace))
                    .thenReturn(Optional.of(newsFromFind));
            when(newsRepository.save(any(News.class)))
                    .thenReturn(newsFromSave);
            when(newsMapper.toResponse(newsFromSave))
                    .thenReturn(newsResponse);

            LocalDateTime timeBeforeTest = LocalDateTime.now();

            // when
            Optional<NewsResponse> updatedOptional = newsServiceImpl.update(idOfNewsToReplace, newsRequest);

            // then
            verify(newsRepository).save(newsCaptor.capture());
            News captured = newsCaptor.getValue();

            assertThat(timeBeforeTest).isBefore(captured.getUpdateTime());
            assertThat(captured.getId()).isNotNull();
            assertThat(captured.getTitle()).isEqualTo(newsRequest.getTitle());
            assertThat(captured.getText()).isEqualTo(newsRequest.getText());

            assertThat(updatedOptional).isPresent();
            assertThat(updatedOptional.get()).isSameAs(newsResponse);
        }

    }
}

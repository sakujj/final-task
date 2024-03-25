package io.github.sakujj.nms.unit.service;

import io.github.sakujj.nms.dto.CommentResponse;
import io.github.sakujj.nms.dto.CommentSaveRequest;
import io.github.sakujj.nms.dto.CommentUpdateRequest;
import io.github.sakujj.nms.entity.Comment;
import io.github.sakujj.nms.entity.NewsId;
import io.github.sakujj.nms.mapper.CommentMapper;
import io.github.sakujj.nms.repository.CommentRepository;
import io.github.sakujj.nms.service.CommentServiceImpl;
import io.github.sakujj.nms.util.CommentTestBuilder;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTests {

    private CommentMapper commentMapper;

    private CommentRepository commentRepository;

    private CommentServiceImpl commentServiceImpl;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Captor
    private ArgumentCaptor<Comment> commentCaptor;

    @BeforeEach
    void initMocks() {
        commentRepository = Mockito.mock(CommentRepository.class);
        commentMapper = Mockito.mock(CommentMapper.class);

        commentServiceImpl = Mockito.spy(new CommentServiceImpl(commentRepository, commentMapper));
    }

    @Nested
    @DisplayName("findById (UUID)")
    class findById {
        @Test
        void shouldFindById() {

            // given
            CommentTestBuilder aComment = CommentTestBuilder.aComment();
            CommentResponse expected = aComment.buildResponse();
            Comment comment = aComment.build();
            UUID idToFindBy = comment.getId();

            when(commentMapper.toResponse(comment))
                    .thenReturn(expected);
            when(commentRepository.findById(idToFindBy))
                    .thenReturn(Optional.of(comment));

            // when
            Optional<CommentResponse> actual = commentServiceImpl.findById(idToFindBy);

            // then
            assertThat(actual).isPresent();
            assertThat(actual.get()).isEqualTo(expected);
        }

        @Test
        void shouldReturnOptionalEmpty_whenNotFoundById() {

            // given
            UUID idToFindBy = CommentTestBuilder.aComment().getId();

            when(commentRepository.findById(idToFindBy))
                    .thenReturn(Optional.empty());

            // when
            Optional<CommentResponse> actual = commentServiceImpl.findById(idToFindBy);

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
            List<Comment> comments = CommentTestBuilder.getTestList().stream()
                    .map(CommentTestBuilder::build)
                    .toList();
            List<CommentResponse> commentResponses = CommentTestBuilder.getTestList().stream()
                    .map(CommentTestBuilder::buildResponse)
                    .toList();
            Page<CommentResponse> expected = new PageImpl<>(commentResponses);

            when(commentRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(comments));
            IntStream.range(0, comments.size())
                    .forEach(i -> {
                        Comment commentToMap = comments.get(i);
                        CommentResponse mappedResponse = commentResponses.get(i);

                        when(commentMapper.toResponse(commentToMap))
                                .thenReturn(mappedResponse);
                    });

            int pageNumber = 3;
            int pageSize = 15;
            String propertyToSortBy = Comment.Fields.CREATION_TIME;

            // when
            Page<CommentResponse> actual = commentServiceImpl.findAll(pageNumber, pageSize);

            // then
            verify(commentRepository).findAll(pageableCaptor.capture());
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

            List<Comment> comments = CommentTestBuilder.getTestList().stream()
                    .map(CommentTestBuilder::build)
                    .toList();
            List<CommentResponse> commentResponses = CommentTestBuilder.getTestList().stream()
                    .map(CommentTestBuilder::buildResponse)
                    .toList();
            Page<CommentResponse> expected = new PageImpl<>(commentResponses);

            when(commentRepository.findByUsernameContaining(eq(containedInUsername), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(comments));
            IntStream.range(0, comments.size())
                    .forEach(i -> {
                        Comment commentToMap = comments.get(i);
                        CommentResponse mappedResponse = commentResponses.get(i);

                        when(commentMapper.toResponse(commentToMap))
                                .thenReturn(mappedResponse);
                    });

            int pageNumber = 3;
            int pageSize = 15;
            String propertyToSortBy = Comment.Fields.CREATION_TIME;

            // when
            Page<CommentResponse> actual = commentServiceImpl.findByUsernameContaining(containedInUsername, pageNumber, pageSize);

            // then
            verify(commentRepository).findByUsernameContaining(eq(containedInUsername), pageableCaptor.capture());
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
    @DisplayName("findByNewsIdId (UUID, int, int)")
    class findByNewsId {

        @Test
        void shouldFindByNewsId() {

            // given
            UUID newsIdUUID = CommentTestBuilder.aComment().getNewsIdUUID();

            Comment containedInRepo = CommentTestBuilder.aComment().build();
            PageImpl<Comment> expectedFromRepo = new PageImpl<>(List.of(containedInRepo));

            CommentResponse mappedFromContainedInRepo = CommentTestBuilder.aComment().buildResponse();
            PageImpl<CommentResponse> expected = new PageImpl<>(List.of(mappedFromContainedInRepo));


            when(commentRepository.findByNewsIdId(
                    eq(newsIdUUID),
                    any(Pageable.class))
            ).thenReturn(expectedFromRepo);

            when(commentMapper.toResponse(containedInRepo))
                    .thenReturn(mappedFromContainedInRepo);

            int pageNumber = 3;
            int pageSize = 15;
            String propertyToSortBy = Comment.Fields.CREATION_TIME;

            // when
            Page<CommentResponse> actual = commentServiceImpl.findByNewsId(
                    newsIdUUID,
                    pageNumber, pageSize);

            // then
            verify(commentRepository).findByNewsIdId(eq(newsIdUUID), pageableCaptor.capture());
            Pageable captured = pageableCaptor.getValue();

            assertThat(captured.getPageNumber()).isEqualTo(pageNumber);
            assertThat(captured.getPageSize()).isEqualTo(pageSize);

            Sort.Order order = captured.getSort().getOrderFor(propertyToSortBy);
            assertThat(order).isNotNull();
            assertThat(order.getDirection().isDescending()).isTrue();

            assertThat(actual).isEqualTo(expected);
        }

    }

    @Nested
    @DisplayName("findByNewsIdIdAndUsernameContaining (UUID, String, int, int)")
    class findByNewsIdAndUsernameContaining {

        @Test
        void shouldFindByNewsIdAndUsernameContainingSortedDescByCreationTime() {

            // given
            UUID newsIdToFindBy = CommentTestBuilder.aComment().getNewsIdUUID();

            String containedInUsername = "some info in username";

            List<Comment> comment = CommentTestBuilder.getTestList().stream()
                    .map(CommentTestBuilder::build)
                    .toList();
            List<CommentResponse> commentResponses = CommentTestBuilder.getTestList().stream()
                    .map(CommentTestBuilder::buildResponse)
                    .toList();
            Page<CommentResponse> expected = new PageImpl<>(commentResponses);

            when(commentRepository.findByNewsIdIdAndUsernameContaining(
                    eq(newsIdToFindBy),
                    eq(containedInUsername),
                    any(Pageable.class))
            ).thenReturn(new PageImpl<>(comment));

            IntStream.range(0, comment.size())
                    .forEach(i -> {
                        Comment commentToMap = comment.get(i);
                        CommentResponse mappedResponse = commentResponses.get(i);

                        when(commentMapper.toResponse(commentToMap))
                                .thenReturn(mappedResponse);
                    });

            int pageNumber = 3;
            int pageSize = 15;
            String propertyToSortBy = Comment.Fields.CREATION_TIME;

            // when
            Page<CommentResponse> actual = commentServiceImpl.findByNewsIdAndUsernameContaining(
                    newsIdToFindBy,
                    containedInUsername,
                    pageNumber, pageSize);

            // then
            verify(commentRepository).findByNewsIdIdAndUsernameContaining(
                    eq(newsIdToFindBy),
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
            UUID id = CommentTestBuilder.aComment().getId();

            // when
            commentServiceImpl.deleteById(id);

            // then
            verify(commentRepository).deleteById(id);
        }
    }

    @Nested
    @DisplayName("create (CommentRequest, UUID, String)")
    class create {

        @Test
        void shouldNotCreate_whenNewsAreNotMappedBecauseDoNotExist() {
            // given
            CommentTestBuilder aComment = CommentTestBuilder.aComment();

            CommentSaveRequest commentRequestToCreate = aComment.buildSaveRequest();

            UUID expectedAuthorId = aComment.getAuthorId();
            String expectedUsername = aComment.getUsername();

            Comment mappedCommentFromRequest = Comment.builder()
                    .newsId(null)
                    .text(commentRequestToCreate.getText())
                    .build();

            when(commentMapper.fromRequest(commentRequestToCreate))
                    .thenReturn(mappedCommentFromRequest);

            // when, then
            assertThatThrownBy(() -> commentServiceImpl.create(commentRequestToCreate, expectedAuthorId, expectedUsername))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldCreate() {

            // given
            CommentTestBuilder aComment = CommentTestBuilder.aComment();

            CommentSaveRequest commentRequestToCreate = aComment.buildSaveRequest();

            UUID expectedAuthorId = aComment.getAuthorId();
            String expectedUsername = aComment.getUsername();

            Comment mappedCommentFromRequest = Comment.builder()
                    .newsId(new NewsId(commentRequestToCreate.getNewsId(), expectedAuthorId))
                    .text(commentRequestToCreate.getText())
                    .build();

            when(commentMapper.fromRequest(commentRequestToCreate))
                    .thenReturn(mappedCommentFromRequest);

            Comment expectedFromRepo = CommentTestBuilder.aComment().build();
            when(commentRepository.save(any(Comment.class)))
                    .thenReturn(expectedFromRepo);

            CommentResponse expected = CommentTestBuilder.aComment().buildResponse();
            when(commentMapper.toResponse(same(expectedFromRepo)))
                    .thenReturn(expected);

            LocalDateTime timeBeforeTest = LocalDateTime.now();

            // when
            CommentResponse actual = commentServiceImpl.create(commentRequestToCreate, expectedAuthorId, expectedUsername);

            // then
            verify(commentRepository).save(commentCaptor.capture());
            Comment commentCaptured = commentCaptor.getValue();

            assertThat(commentCaptured.getCreationTime()).isAfter(timeBeforeTest);
            assertThat(commentCaptured.getUpdateTime()).isAfter(timeBeforeTest);
            assertThat(commentCaptured.getId()).isNotNull();

            assertThat(commentCaptured.getUsername()).isEqualTo(expectedUsername);
            assertThat(commentCaptured.getAuthorId()).isEqualTo(expectedAuthorId);
            assertThat(commentCaptured.getText()).isEqualTo(commentRequestToCreate.getText());
            assertThat(commentCaptured.getNewsId().getId()).isEqualTo(commentRequestToCreate.getNewsId());

            assertThat(actual).isSameAs(expected);
        }
    }

    @Nested
    @DisplayName("update (UUID, CommentRequest)")
    class update {

        @Test
        void shouldReturnEmptyOptional_whenNotFound_onUpdate() {

            // given
            CommentTestBuilder aComment = CommentTestBuilder.aComment();

            CommentUpdateRequest commentRequest = aComment.buildUpdateRequest();
            UUID idOfCommentToReplace = aComment.getId();

            when(commentRepository.findById(idOfCommentToReplace))
                    .thenReturn(Optional.empty());

            // when
            Optional<CommentResponse> updatedOptional = commentServiceImpl.update(idOfCommentToReplace, commentRequest);

            // then
            assertThat(updatedOptional).isEmpty();
        }

        @Test
        void shouldUpdate() {

            // given
            CommentUpdateRequest commentRequest = CommentTestBuilder.aComment()
                    .withText("some new text vcvxcfgx")
                    .buildUpdateRequest();

            UUID idOfCommentToReplace = CommentTestBuilder.aComment().getId();

            Comment commentFromFind = CommentTestBuilder.aComment().build();
            CommentResponse commentResponse = CommentTestBuilder.aComment().buildResponse();

            Comment commentFromSave = CommentTestBuilder.aComment().build();

            when(commentRepository.findById(idOfCommentToReplace))
                    .thenReturn(Optional.of(commentFromFind));
            when(commentRepository.save(any(Comment.class)))
                    .thenReturn(commentFromSave);
            when(commentMapper.toResponse(commentFromSave))
                    .thenReturn(commentResponse);

            LocalDateTime timeBeforeTest = LocalDateTime.now();

            // when
            Optional<CommentResponse> updatedOptional = commentServiceImpl.update(idOfCommentToReplace, commentRequest);

            // then
            verify(commentRepository).save(commentCaptor.capture());
            Comment captured = commentCaptor.getValue();

            assertThat(timeBeforeTest).isBefore(captured.getUpdateTime());
            assertThat(captured.getId()).isNotNull();
            assertThat(captured.getText()).isEqualTo(commentRequest.getText());

            assertThat(updatedOptional).isPresent();
            assertThat(updatedOptional.get()).isSameAs(commentResponse);
        }

    }
}

package io.github.sakujj.nms.integration.service;

import io.github.sakujj.nms.dto.CommentUpdateRequest;
import io.github.sakujj.nms.entity.Comment;
import io.github.sakujj.nms.entity.NewsId;
import io.github.sakujj.nms.integration.testcontainer.ExclusivePostgresContainerInitializer;
import io.github.sakujj.nms.mapper.CommentMapper;
import io.github.sakujj.nms.repository.CommentRepository;
import io.github.sakujj.nms.repository.NewsIdRepository;
import io.github.sakujj.nms.service.CommentServiceImpl;
import io.github.sakujj.nms.util.CommentTestBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@ActiveProfiles("test")
public class CommentServiceImplTests extends ExclusivePostgresContainerInitializer {

    @Autowired
    private CommentServiceImpl commentServiceImpl;

    @SpyBean
    private CommentRepository commentRepository;

    @Autowired
    private NewsIdRepository newsIdRepository;

    @Autowired
    private CommentMapper commentMapper;

    @PersistenceContext
    private EntityManager entityManager;


    @Test
    // specifying isolation REPEATABLE_READ, because tested method has isolation REPEATABLE_READ and propagation REQUIRED,
    // and here were are testing that due to REPEATABLE_READ we should be notified about concurrent delete modification.
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void shouldThrowException_dueToConcurrentDeleteInRepeatableRead_onUpdate() throws ExecutionException, InterruptedException {

        // GIVEN
        NewsId newsId = new NewsId(CommentTestBuilder.aComment().getNewsIdUUID(), CommentTestBuilder.aComment().getAuthorId());
        Comment commentToUpdate = CommentTestBuilder.aComment()
                .withNewsId(newsId)
                .build();

        // saving comment from other transaction, before the start of the current one
        CompletableFuture.runAsync(() -> {
            newsIdRepository.save(commentToUpdate.getNewsId());
            commentRepository.save(commentToUpdate);
        }).get();

        CommentUpdateRequest commentUpdateRequest = CommentTestBuilder.aComment().buildUpdateRequest();

        Answer<?> defaultAnswer = Mockito
                .mockingDetails(commentRepository)
                .getMockCreationSettings()
                .getDefaultAnswer();

        doAnswer(invocation ->
                {
                    // calling the real method
                    Optional<Comment> foundById = (Optional<Comment>) defaultAnswer.answer(invocation);

                    // simulating concurrent deleteById from other transaction
                    CompletableFuture.runAsync(() -> commentRepository.deleteById(commentToUpdate.getId())).get();

                    return foundById;
                }
        ).when(commentRepository).findById(commentToUpdate.getId());

        // WHEN, THEN
        assertThatThrownBy(() ->
                {
                    // starting current transaction
                    commentServiceImpl.update(commentToUpdate.getId(), commentUpdateRequest);

                    entityManager.flush();
                }
        ).isInstanceOfSatisfying(PersistenceException.class, e ->
        {
            PSQLException psqlEx = extractPSQLExceptionOrReturnNullOnFail(e);
            assertThat((Object) psqlEx).isNotNull();

            String errorCode = psqlEx.getSQLState();
            assertThat(errorCode).isEqualTo(PSQLState.SERIALIZATION_FAILURE.getState());
        });

    }

    private static <T extends RuntimeException> PSQLException extractPSQLExceptionOrReturnNullOnFail(T exception) {

        Throwable throwable = exception;

        while ((throwable != null) && !(throwable instanceof PSQLException)) {
            throwable = throwable.getCause();
        }

        return (PSQLException) throwable;
    }
}

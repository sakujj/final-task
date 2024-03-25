package io.github.sakujj.nms.integration.service;

import io.github.sakujj.nms.config.CachingConfig;
import io.github.sakujj.nms.dto.NewsRequest;
import io.github.sakujj.nms.entity.News;
import io.github.sakujj.nms.integration.testcontainer.ExclusivePostgresContainerInitializer;
import io.github.sakujj.nms.mapper.NewsMapper;
import io.github.sakujj.nms.repository.NewsRepository;
import io.github.sakujj.nms.service.NewsServiceImpl;
import io.github.sakujj.nms.util.NewsTestBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
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
public class NewsServiceImplTests extends ExclusivePostgresContainerInitializer {

    @Autowired
    private NewsServiceImpl newsServiceImpl;

    @SpyBean
    private NewsRepository newsRepository;

    @Autowired
    private NewsMapper newsMapper;

    @PersistenceContext
    private EntityManager entityManager;


    @Test
    // specifying isolation REPEATABLE_READ, because tested method has isolation REPEATABLE_READ and propagation REQUIRED,
    // and here were are testing that due to REPEATABLE_READ we should be notified about concurrent delete modification.
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void shouldThrowException_dueToConcurrentDeleteInRepeatableRead_onUpdate() throws ExecutionException, InterruptedException {

        // GIVEN
        News newsToUpdate = NewsTestBuilder.anArticle().build();

        // saving news from other transaction, before the start of the current one
        CompletableFuture.runAsync(() -> newsRepository.save(newsToUpdate)).get();

        NewsRequest newsUpdateRequest = NewsTestBuilder.anArticle().buildRequest();

        Answer<?> defaultAnswer = Mockito
                .mockingDetails(newsRepository)
                .getMockCreationSettings()
                .getDefaultAnswer();

        doAnswer(invocation ->
                {
                    // calling the real method
                    Optional<News> foundById = (Optional<News>) defaultAnswer.answer(invocation);

                    // simulating concurrent deleteById from other transaction
                    CompletableFuture.runAsync(() -> newsRepository.deleteById(newsToUpdate.getId())).get();

                    return foundById;
                }
        ).when(newsRepository).findById(newsToUpdate.getId());

        // WHEN, THEN
        assertThatThrownBy(() ->
                {
                    // starting current transaction
                    newsServiceImpl.update(newsToUpdate.getId(), newsUpdateRequest);

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

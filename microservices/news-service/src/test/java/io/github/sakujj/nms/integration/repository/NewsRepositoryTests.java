package io.github.sakujj.nms.integration.repository;

import io.github.sakujj.nms.entity.News;
import io.github.sakujj.nms.integration.testcontainer.CommonPostgresContainerInitializer;
import io.github.sakujj.nms.repository.NewsRepository;
import io.github.sakujj.nms.util.NewsTestBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class NewsRepositoryTests extends CommonPostgresContainerInitializer {

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    TestEntityManager testEntityManager;

    @Test
    void shouldFindAll() {

        // given
        News newsFirst = NewsTestBuilder.anArticle()
                .build();
        News newsSecond = NewsTestBuilder.anArticle()
                .withId(UUID.fromString("462f5504-2550-4bc1-9c3f-61696a622de7"))
                .build();
        News newsThird = NewsTestBuilder.anArticle()
                .withId(UUID.fromString("64336a66-4409-47cc-a8f7-0ef977f1cb57"))
                .build();
        News newsFourth = NewsTestBuilder.anArticle()
                .withId(UUID.fromString("888f9028-79a6-442a-9e74-129e3f9b7595"))
                .build();

        testEntityManager.persist(newsFirst);
        testEntityManager.persist(newsSecond);
        testEntityManager.persist(newsThird);
        testEntityManager.persist(newsFourth);

        Pageable pageable = PageRequest.of(0, 20 + 4);

        // when
        testEntityManager.flush();

        List<News> actual = newsRepository.findAll(pageable).getContent();

        // then
        assertThat(actual).contains(newsFirst, newsSecond, newsThird, newsFourth);
    }

    @Test
    void shouldFindByTitleContaining() {

        // given
        News newsFirst = NewsTestBuilder.anArticle()
                .build();
        News newsSecond = NewsTestBuilder.anArticle()
                .withId(UUID.fromString("462f5504-2550-4bc1-9c3f-61696a622de7"))
                .withTitle("title 2 ###")
                .build();
        News newsThird = NewsTestBuilder.anArticle()
                .withId(UUID.fromString("64336a66-4409-47cc-a8f7-0ef977f1cb57"))
                .withTitle("tit ## le 3")
                .build();
        News newsFourth = NewsTestBuilder.anArticle()
                .withId(UUID.fromString("888f9028-79a6-442a-9e74-129e3f9b7595"))
                .withTitle("titl # # e 4")
                .build();

        testEntityManager.persist(newsFirst);
        testEntityManager.persist(newsSecond);
        testEntityManager.persist(newsThird);
        testEntityManager.persist(newsFourth);

        Pageable pageable = PageRequest.of(0, 10);

        String containedInTitle = "##";

        // when
        testEntityManager.flush();

        List<News> actual = newsRepository.findByTitleContaining(containedInTitle, pageable).getContent();

        // then
        assertThat(actual).containsOnly(newsSecond, newsThird);
    }

    @Test
    void shouldFindByUsernameContaining() {

        // given
        News newsFirst = NewsTestBuilder.anArticle()
                .withUsername("u$$$er 1")
                .build();
        News newsSecond = NewsTestBuilder.anArticle()
                .withId(UUID.fromString("462f5504-2550-4bc1-9c3f-61696a622de7"))
                .withUsername("user @@$$$$2 ###")
                .build();
        News newsThird = NewsTestBuilder.anArticle()
                .withId(UUID.fromString("64336a66-4409-47cc-a8f7-0ef977f1cb57"))
                .withUsername("user #$# $ le 3")
                .build();
        News newsFourth = NewsTestBuilder.anArticle()
                .withId(UUID.fromString("888f9028-79a6-442a-9e74-129e3f9b7595"))
                .withUsername("us # #$$ e r 4")
                .build();

        testEntityManager.persist(newsFirst);
        testEntityManager.persist(newsSecond);
        testEntityManager.persist(newsThird);
        testEntityManager.persist(newsFourth);

        Pageable pageable = PageRequest.of(0, 10);

        String containedInUsername = "$$";

        // when
        testEntityManager.flush();

        List<News> actual = newsRepository.findByUsernameContaining(containedInUsername, pageable).getContent();

        // then
        assertThat(actual).containsOnly(newsFirst, newsSecond, newsFourth);
    }


    @Test
    void shouldFindByTitleContainingAndUsernameContaining() {

        // given
        News newsFirst = NewsTestBuilder.anArticle()
                .withUsername("u$$$er 1")
                .withTitle("tit###le 1")
                .build();
        News newsSecond = NewsTestBuilder.anArticle()
                .withId(UUID.fromString("462f5504-2550-4bc1-9c3f-61696a622de7"))
                .withUsername("user @@$$$$2 ###")
                .withTitle("title 2")
                .build();
        News newsThird = NewsTestBuilder.anArticle()
                .withId(UUID.fromString("64336a66-4409-47cc-a8f7-0ef977f1cb57"))
                .withUsername("user #$# $ le 3")
                .withTitle("title 3 ##")
                .build();
        News newsFourth = NewsTestBuilder.anArticle()
                .withId(UUID.fromString("888f9028-79a6-442a-9e74-129e3f9b7595"))
                .withUsername("us # #$$ e r 4")
                .withTitle("##title 4##")
                .build();

        testEntityManager.persist(newsFirst);
        testEntityManager.persist(newsSecond);
        testEntityManager.persist(newsThird);
        testEntityManager.persist(newsFourth);

        Pageable pageable = PageRequest.of(0, 10);

        String containedInTitle = "##";
        String containedInUsername = "$$";

        // when
        testEntityManager.flush();

        List<News> actual = newsRepository.findByTitleContainingAndUsernameContaining(
                        containedInTitle,
                        containedInUsername,
                        pageable)
                .getContent();

        // then
        assertThat(actual).containsOnly(newsFirst, newsFourth);
    }

    @Test
    void shouldFindNewsById() {

        // given
        News newsToFind = NewsTestBuilder.anArticle()
                .build();
        testEntityManager.persistAndFlush(newsToFind);

        // when
        Optional<News> actualOptional = newsRepository.findById(newsToFind.getId());

        // then
        assertThat(actualOptional).isPresent();
        assertThat(actualOptional.get()).isEqualTo(newsToFind);
    }

    @Test
    void shouldSaveNews() {

        // given
        News newsToSave = NewsTestBuilder.anArticle()
                .build();

        // when
        News newsSaved = newsRepository.save(newsToSave);

        testEntityManager.flush();

        News actual = testEntityManager.find(News.class, newsToSave.getId());

        // then
        assertThat(newsSaved).isEqualTo(newsToSave);
        assertThat(actual).isEqualTo(newsToSave);
    }

    @Test
    void shouldDeleteNews() {

        // given
        News newsToDelete = NewsTestBuilder.anArticle()
                .build();
        testEntityManager.persistAndFlush(newsToDelete);

        // when
        newsRepository.deleteById(newsToDelete.getId());

        testEntityManager.flush();

        News actual = testEntityManager.find(News.class, newsToDelete.getId());

        // then
        assertThat(actual).isNull();
    }
}

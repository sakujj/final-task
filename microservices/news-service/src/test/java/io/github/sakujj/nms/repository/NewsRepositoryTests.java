package io.github.sakujj.nms.repository;

import io.github.sakujj.nms.entity.News;
import io.github.sakujj.nms.testcontainer.CommonPostgresContainerInitializer;
import io.github.sakujj.nms.util.NewsTestBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class NewsRepositoryTests extends CommonPostgresContainerInitializer {

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    TestEntityManager testEntityManager;

    @Test
    @Transactional
    void shouldUpdateNewsById() {

        // given
        News news = NewsTestBuilder.anArticle()
                .withId(null)
                .build();
        News persisted = testEntityManager.persist(news);

        String titleUpdated = "Updated title";
        String textUpdated = "Updated text";
        LocalDateTime updateTimeUpdated = LocalDateTime.of(2024, Month.MARCH, 1, 19, 0);

        News expected = NewsTestBuilder.anArticle()
                .withId(persisted.getId())
                .withText(textUpdated)
                .withTitle(titleUpdated)
                .withUpdateTime(updateTimeUpdated)

                .build();

        // when
        int updatedRowsCount = newsRepository.updateById(news.getId(), textUpdated, titleUpdated, updateTimeUpdated);

        News actual = testEntityManager.find(News.class, news.getId());

        // then
        assertThat(updatedRowsCount).isEqualTo(1);
        assertThat(actual).isEqualTo(expected);
    }
}

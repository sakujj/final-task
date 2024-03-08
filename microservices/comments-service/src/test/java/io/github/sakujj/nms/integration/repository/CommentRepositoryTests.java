package io.github.sakujj.nms.integration.repository;

import io.github.sakujj.nms.entity.Comment;
import io.github.sakujj.nms.entity.NewsId;
import io.github.sakujj.nms.integration.testcontainer.CommonPostgresContainerInitializer;
import io.github.sakujj.nms.repository.CommentRepository;
import io.github.sakujj.nms.repository.NewsIdRepository;
import io.github.sakujj.nms.util.CommentTestBuilder;
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
public class CommentRepositoryTests extends CommonPostgresContainerInitializer {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    TestEntityManager testEntityManager;

    @Test
    void shouldFindAll() {

        // given
        UUID someAuthorId = CommentTestBuilder.aComment().getAuthorId();

        NewsId someNewsId = new NewsId(UUID.fromString("81e152be-2ad8-489a-8959-943bdea8e351"), someAuthorId);
        testEntityManager.persist(someNewsId);
        NewsId someOtherNewsId = new NewsId(UUID.fromString("67c9e308-1347-40e7-be53-35cefdb9273c"), someAuthorId);
        testEntityManager.persist(someOtherNewsId);

        testEntityManager.flush();

        Comment commentFirst = CommentTestBuilder.aComment()
                .withNewsId(someNewsId)
                .build();
        Comment commentSecond = CommentTestBuilder.aComment()
                .withId(UUID.fromString("462f5504-2550-4bc1-9c3f-61696a622de7"))
                .withNewsId(someOtherNewsId)
                .build();
        Comment commentThird = CommentTestBuilder.aComment()
                .withId(UUID.fromString("64336a66-4409-47cc-a8f7-0ef977f1cb57"))
                .withNewsId(someNewsId)
                .build();
        Comment commentFourth = CommentTestBuilder.aComment()
                .withId(UUID.fromString("888f9028-79a6-442a-9e74-129e3f9b7595"))
                .withNewsId(someNewsId)
                .build();

        testEntityManager.persist(commentFirst);
        testEntityManager.persist(commentSecond);
        testEntityManager.persist(commentThird);
        testEntityManager.persist(commentFourth);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        testEntityManager.flush();
        testEntityManager.clear();

        List<Comment> actual = commentRepository.findAll(pageable).getContent();

        // then
        assertThat(actual).containsOnly(commentFirst, commentSecond, commentThird, commentFourth);
    }

    @Test
    void shouldFindByNewsId() {

        // given
        UUID someAuthorId = CommentTestBuilder.aComment().getAuthorId();
        UUID newsIdToFindBy = UUID.fromString("49eddfd4-715a-4488-b53d-dbe76d8d0f9f");
        NewsId newsIdEntity = new NewsId(newsIdToFindBy, someAuthorId);
        NewsId someNewsIdEntity = new NewsId(UUID.fromString("81e152be-2ad8-489a-8959-943bdea8e351"), someAuthorId);

        testEntityManager.persist(newsIdEntity);
        testEntityManager.persist(someNewsIdEntity);
        testEntityManager.flush();

        Comment commentFirst = CommentTestBuilder.aComment()
                .withNewsId(someNewsIdEntity)
                .build();
        Comment commentSecond = CommentTestBuilder.aComment()
                .withId(UUID.fromString("462f5504-2550-4bc1-9c3f-61696a622de7"))
                .withNewsId(newsIdEntity)
                .build();
        Comment commentThird = CommentTestBuilder.aComment()
                .withId(UUID.fromString("64336a66-4409-47cc-a8f7-0ef977f1cb57"))
                .withNewsId(newsIdEntity)
                .build();
        Comment commentFourth = CommentTestBuilder.aComment()
                .withId(UUID.fromString("888f9028-79a6-442a-9e74-129e3f9b7595"))
                .withNewsId(someNewsIdEntity)
                .build();

        testEntityManager.persist(commentFirst);
        testEntityManager.persist(commentSecond);
        testEntityManager.persist(commentThird);
        testEntityManager.persist(commentFourth);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        testEntityManager.flush();
        testEntityManager.clear();

        List<Comment> actual = commentRepository.findByNewsIdId(newsIdToFindBy, pageable).getContent();

        // then
        assertThat(actual).containsOnly(commentSecond, commentThird);
    }

    @Test
    void shouldFindByUsernameContaining() {

        // given
        UUID someAuthorId = CommentTestBuilder.aComment().getAuthorId();

        NewsId someNewsId = new NewsId(UUID.fromString("81e152be-2ad8-489a-8959-943bdea8e351"),someAuthorId);
        testEntityManager.persist(someNewsId);
        NewsId someOtherNewsId = new NewsId(UUID.fromString("67c9e308-1347-40e7-be53-35cefdb9273c"), someAuthorId);
        testEntityManager.persist(someOtherNewsId);
        testEntityManager.flush();

        Comment commentFirst = CommentTestBuilder.aComment()
                .withNewsId(someNewsId)
                .withUsername("username_$$_someusername")
                .build();
        Comment commentSecond = CommentTestBuilder.aComment()
                .withNewsId(someOtherNewsId)
                .withId(UUID.fromString("462f5504-2550-4bc1-9c3f-61696a622de7"))
                .withUsername("Userxxx$$$$$")
                .build();
        Comment commentThird = CommentTestBuilder.aComment()
                .withNewsId(someNewsId)
                .withId(UUID.fromString("64336a66-4409-47cc-a8f7-0ef977f1cb57"))
                .withUsername("username_3")
                .build();
        Comment commentFourth = CommentTestBuilder.aComment()
                .withNewsId(someNewsId)
                .withId(UUID.fromString("888f9028-79a6-442a-9e74-129e3f9b7595"))
                .withUsername("$$$uzzzer$")
                .build();

        testEntityManager.persist(commentFirst);
        testEntityManager.persist(commentSecond);
        testEntityManager.persist(commentThird);
        testEntityManager.persist(commentFourth);

        Pageable pageable = PageRequest.of(0, 10);

        String containedInUsername = "$$";

        // when
        testEntityManager.flush();
        testEntityManager.clear();

        List<Comment> actual = commentRepository.findByUsernameContaining(containedInUsername, pageable).getContent();

        // then
        assertThat(actual).containsOnly(commentFirst, commentSecond, commentFourth);
    }

    @Test
    void shouldFindByNewsIdAndUsernameContaining() {

        // given
        UUID someAuthorId = CommentTestBuilder.aComment().getAuthorId();

        UUID newsIdToFindBy = UUID.fromString("49eddfd4-715a-4488-b53d-dbe76d8d0f9f");
        NewsId newsIdEntity = new NewsId(newsIdToFindBy, someAuthorId);
        NewsId someNewsIdEntity = new NewsId(UUID.fromString("81e152be-2ad8-489a-8959-943bdea8e351"), someAuthorId);

        testEntityManager.persist(newsIdEntity);
        testEntityManager.persist(someNewsIdEntity);
        testEntityManager.flush();

        Comment commentFirst = CommentTestBuilder.aComment()
                .withUsername("username_$$_someusername")
                .withNewsId(newsIdEntity)
                .build();
        Comment commentSecond = CommentTestBuilder.aComment()
                .withId(UUID.fromString("462f5504-2550-4bc1-9c3f-61696a622de7"))
                .withNewsId(someNewsIdEntity)
                .withUsername("Userxxx$$$$$")
                .build();
        Comment commentThird = CommentTestBuilder.aComment()
                .withId(UUID.fromString("64336a66-4409-47cc-a8f7-0ef977f1cb57"))
                .withNewsId(someNewsIdEntity)
                .withUsername("username_3")
                .build();
        Comment commentFourth = CommentTestBuilder.aComment()
                .withNewsId(newsIdEntity)
                .withId(UUID.fromString("888f9028-79a6-442a-9e74-129e3f9b7595"))
                .withUsername("$$$uzzzer$")
                .build();

        testEntityManager.persist(commentFirst);
        testEntityManager.persist(commentSecond);
        testEntityManager.persist(commentThird);
        testEntityManager.persist(commentFourth);

        Pageable pageable = PageRequest.of(0, 10);

        String containedInUsername = "$$";

        // when
        testEntityManager.flush();

        List<Comment> actual = commentRepository.findByNewsIdIdAndUsernameContaining(
                        newsIdToFindBy,
                        containedInUsername,
                        pageable)
                .getContent();

        // then
        assertThat(actual).containsOnly(commentFirst, commentFourth);
    }

    @Test
    void shouldFindCommentById() {

        // given
        UUID someAuthorId = CommentTestBuilder.aComment().getAuthorId();
        NewsId someNewsId = new NewsId(UUID.fromString("81e152be-2ad8-489a-8959-943bdea8e351"), someAuthorId);
        testEntityManager.persist(someNewsId);

        Comment commentToFind = CommentTestBuilder.aComment()
                .withNewsId(someNewsId)
                .build();
        testEntityManager.persist(commentToFind);
        testEntityManager.flush();

        // when
        Optional<Comment> actualOptional = commentRepository.findById(commentToFind.getId());

        // then
        assertThat(actualOptional).isPresent();
        assertThat(actualOptional.get()).isEqualTo(commentToFind);
    }

    @Test
    void shouldSaveComment() {

        // given
        UUID someAuthorId = CommentTestBuilder.aComment().getAuthorId();
        NewsId someNewsId = new NewsId(UUID.fromString("81e152be-2ad8-489a-8959-943bdea8e351"), someAuthorId);
        testEntityManager.persist(someNewsId);
        testEntityManager.flush();

        Comment commentToSave = CommentTestBuilder.aComment()
                .withNewsId(someNewsId)
                .build();

        // when
        Comment commentSaved = commentRepository.save(commentToSave);

        testEntityManager.flush();

        Comment actual = testEntityManager.find(Comment.class, commentToSave.getId());

        // then
        assertThat(commentSaved).isEqualTo(commentToSave);
        assertThat(actual).isEqualTo(commentToSave);
    }

    @Test
    void shouldDeleteComment() {

        // given
        UUID someAuthorId = CommentTestBuilder.aComment().getAuthorId();

        NewsId someNewsId = new NewsId(UUID.fromString("81e152be-2ad8-489a-8959-943bdea8e351"), someAuthorId);
        testEntityManager.persist(someNewsId);

        Comment commentToDelete = CommentTestBuilder.aComment()
                .withNewsId(someNewsId)
                .build();
        testEntityManager.persist(commentToDelete);
        testEntityManager.flush();

        // when
        commentRepository.deleteById(commentToDelete.getId());

        testEntityManager.flush();

        Comment actual = testEntityManager.find(Comment.class, commentToDelete.getId());

        // then
        assertThat(actual).isNull();
    }
}

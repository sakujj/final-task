package io.github.sakujj.nms.repository;

import io.github.sakujj.nms.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @EntityGraph(attributePaths = {"newsId"})
    Page<Comment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"newsId"})
    Page<Comment> findByUsernameContaining(String containedInUsername, Pageable pageable);

    @EntityGraph(attributePaths = {"newsId"})
    Page<Comment> findByNewsIdId(UUID newsId, Pageable pageable);

    @EntityGraph(attributePaths = {"newsId"})
    Page<Comment> findByNewsIdIdAndUsernameContaining(UUID newsId, String containedInUsername, Pageable pageable);
}

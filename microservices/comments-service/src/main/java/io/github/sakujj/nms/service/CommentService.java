package io.github.sakujj.nms.service;

import io.github.sakujj.nms.dto.CommentResponse;
import io.github.sakujj.nms.dto.CommentSaveRequest;
import io.github.sakujj.nms.dto.CommentUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface CommentService {

    Optional<CommentResponse> findById(UUID id);

    Page<CommentResponse> findAll(int pageNumber, int pageSize);

    Page<CommentResponse> findByNewsId(UUID newsId, int pageNumber, int pageSize);

    Page<CommentResponse> findByUsernameContaining(String containedInUsername, int pageNumber, int pageSize);

    Page<CommentResponse> findByNewsIdAndUsernameContaining(UUID newsId, String containedInUsername,
                                                            int pageNumber, int pageSize);

    CommentResponse create(CommentSaveRequest commentSaveRequest, UUID authorId, String username);

    void deleteById(UUID id);

    Optional<CommentResponse> update(UUID id, CommentUpdateRequest commentUpdateRequest);
}

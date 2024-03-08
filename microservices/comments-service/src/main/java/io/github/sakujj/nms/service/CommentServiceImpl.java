package io.github.sakujj.nms.service;

import io.github.sakujj.cache.aop.CacheableCreate;
import io.github.sakujj.cache.aop.CacheableDeleteByUUID;
import io.github.sakujj.cache.aop.CacheableFindByUUID;
import io.github.sakujj.cache.aop.CacheableUpdateByUUID;
import io.github.sakujj.nms.dto.CommentResponse;
import io.github.sakujj.nms.dto.CommentSaveRequest;
import io.github.sakujj.nms.dto.CommentUpdateRequest;
import io.github.sakujj.nms.entity.Comment;
import io.github.sakujj.nms.mapper.CommentMapper;
import io.github.sakujj.nms.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    @CacheableFindByUUID
    public Optional<CommentResponse> findById(UUID id) {
        return commentRepository
                .findById(id)
                .map(commentMapper::toResponse);
    }

    @Override
    public Page<CommentResponse> findAll(int pageNumber, int pageSize) {

        Sort timeSort = Sort.by(Sort.Direction.DESC, Comment.Fields.CREATION_TIME);
        Pageable pageableSorted = PageRequest.of(pageNumber, pageSize, timeSort);

        return commentRepository
                .findAll(pageableSorted)
                .map(commentMapper::toResponse);
    }

    @Override
    public Page<CommentResponse> findByNewsId(UUID newsId, int pageNumber, int pageSize) {

        Sort timeSort = Sort.by(Sort.Direction.DESC, Comment.Fields.CREATION_TIME);
        Pageable pageableSorted = PageRequest.of(pageNumber, pageSize, timeSort);

        return commentRepository
                .findByNewsIdId(newsId, pageableSorted)
                .map(commentMapper::toResponse);
    }

    @Override
    public Page<CommentResponse> findByUsernameContaining(String containedInUsername, int pageNumber, int pageSize) {

        Sort timeSort = Sort.by(Sort.Direction.DESC, Comment.Fields.CREATION_TIME);
        Pageable pageableSorted = PageRequest.of(pageNumber, pageSize, timeSort);

        return commentRepository
                .findByUsernameContaining(containedInUsername, pageableSorted)
                .map(commentMapper::toResponse);
    }

    @Override
    public Page<CommentResponse> findByNewsIdAndUsernameContaining(UUID newsId, String containedInUsername,
                                                                   int pageNumber, int pageSize) {

        Sort timeSort = Sort.by(Sort.Direction.DESC, Comment.Fields.CREATION_TIME);
        Pageable pageableSorted = PageRequest.of(pageNumber, pageSize, timeSort);

        return commentRepository
                .findByNewsIdIdAndUsernameContaining(newsId, containedInUsername, pageableSorted)
                .map(commentMapper::toResponse);
    }

    @Override
    @CacheableCreate
    @Transactional
    public CommentResponse create(CommentSaveRequest commentSaveRequest, UUID authorId, String username) {

        Comment commentToSave = commentMapper.fromRequest(commentSaveRequest);
        if (commentToSave.getNewsId() == null) {
            throw new IllegalStateException("News with requested newsId do not exist.");
        }

        LocalDateTime currentTime = LocalDateTime.now();
        UUID uuidGenerated = UUID.randomUUID();

        commentToSave.setId(uuidGenerated);
        commentToSave.setCreationTime(currentTime);
        commentToSave.setUpdateTime(currentTime);
        commentToSave.setAuthorId(authorId);
        commentToSave.setUsername(username);

        Comment commentSaved = commentRepository.save(commentToSave);

        return commentMapper.toResponse(commentSaved);
    }

    @Override
    @Transactional
    @CacheableDeleteByUUID
    public void deleteById(UUID id) {
        commentRepository.deleteById(id);
    }


    @Override
    @CacheableUpdateByUUID
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Optional<CommentResponse> update(UUID id, CommentUpdateRequest commentUpdateRequest) {

        Optional<Comment> commentOptional = commentRepository.findById(id);
        if (commentOptional.isEmpty()) {
            return Optional.empty();
        }

        Comment comment = commentOptional.get();

        String textUpdated = commentUpdateRequest.getText();
        LocalDateTime updateTime = LocalDateTime.now();

        comment.setText(textUpdated);
        comment.setUpdateTime(updateTime);

        Comment commentReplaced = commentRepository.save(comment);

        CommentResponse commentResponse = commentMapper.toResponse(commentReplaced);

        return Optional.of(commentResponse);
    }
}

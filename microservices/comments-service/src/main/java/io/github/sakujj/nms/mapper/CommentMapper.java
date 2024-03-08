package io.github.sakujj.nms.mapper;

import io.github.sakujj.nms.dto.CommentSaveRequest;
import io.github.sakujj.nms.dto.CommentResponse;
import io.github.sakujj.nms.entity.Comment;
import io.github.sakujj.nms.entity.NewsId;
import io.github.sakujj.nms.repository.NewsIdRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", imports = {NewsId.class})
public abstract class CommentMapper {

    @Autowired
    protected NewsIdRepository newsIdRepository;

    @Mapping(target = Comment.Fields.ID, ignore = true)
    @Mapping(target = Comment.Fields.CREATION_TIME, ignore = true)
    @Mapping(target = Comment.Fields.UPDATE_TIME, ignore = true)
    @Mapping(target = Comment.Fields.AUTHOR_ID, ignore = true)
    @Mapping(target = Comment.Fields.USERNAME, ignore = true)
    @Mapping(target = Comment.Fields.NEWS_ID, expression = """
            java(
                this.newsIdRepository.findById(request.getNewsId())
                    .orElseGet(() -> null)
            )""")
    public abstract Comment fromRequest(CommentSaveRequest request);

    @Mapping(target = CommentResponse.Fields.NEWS_ID, expression = "java(entity.getNewsId().getId())")
    public abstract CommentResponse toResponse(Comment entity);
}

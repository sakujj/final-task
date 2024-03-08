package io.github.sakujj.nms.external.dto;

import io.github.sakujj.cache.IdentifiableByUUID;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CommentResponse {

    private UUID id;

    private String text;

    private LocalDateTime creationTime;

    private LocalDateTime updateTime;

    private String username;

    private UUID newsId;

    private UUID authorId;

    public static final class Fields {
        public static final String ID = "id";
        public static final String TEXT = "text";
        public static final String CREATION_TIME = "creationTime";
        public static final String UPDATE_TIME = "updateTime";
        public static final String USERNAME = "username";
        public static final String NEWS_ID = "newsId";
        public static final String AUTHOR_ID = "authorId";
    }
}

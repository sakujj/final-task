package io.github.sakujj.nms.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "comment")
public class Comment {

    @Id
    @Column(name = Columns.ID)
    private UUID id;

    @Column(name = Columns.TEXT, nullable = false)
    private String text;

    @Column(name = Columns.CREATION_TIME, nullable = false)
    private LocalDateTime creationTime;

    @Column(name = Columns.UPDATE_TIME, nullable = false)
    private LocalDateTime updateTime;

    @Column(name = Columns.USERNAME, nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "news_id")
    private NewsId newsId;

    @Column(name = Columns.AUTHOR_ID, updatable = false, nullable = false)
    private UUID authorId;

    public static final class Columns {
        public static final String ID = "id";
        public static final String TEXT = "text";
        public static final String CREATION_TIME = "creation_time";
        public static final String UPDATE_TIME = "update_time";
        public static final String USERNAME = "username";
        public static final String NEWS_ID = "news_id";
        public static final String AUTHOR_ID = "author_id";
    }

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


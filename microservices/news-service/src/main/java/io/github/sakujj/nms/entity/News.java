package io.github.sakujj.nms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "News")
public class News {

    public static final String ID_COLUMN_NAME = "id";
    public static final String TEXT_COLUMN_NAME = "text";
    public static final String CREATION_TIME_COLUMN_NAME = "creation_time";
    public static final String UPDATE_TIME_COLUMN_NAME = "update_time";
    public static final String TITLE_COLUMN_NAME = "title";
    public static final String USERNAME_COLUMN_NAME = "username";
    public static final String AUTHOR_ID_COLUMN_NAME = "author_id";

    @Id
    @Column(name = Columns.ID)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = Columns.TITLE, nullable = false)
    private String title;

    @Column(name = Columns.TEXT, nullable = false)
    private String text;

    @Column(name = Columns.CREATION_TIME, updatable = false, nullable = false)
    private LocalDateTime creationTime;

    @Column(name = Columns.UPDATE_TIME, nullable = false)
    private LocalDateTime updateTime;

    @Column(name = Columns.USERNAME, nullable = false)
    private String username;

    @Column(name = Columns.AUTHOR_ID, updatable = false, nullable = false)
    private UUID authorId;

    public static final class Columns {
        public static final String ID = "id";
        public static final String TITLE = "title";
        public static final String TEXT = "text";
        public static final String CREATION_TIME = "creation_time";
        public static final String UPDATE_TIME = "update_time";
        public static final String USERNAME = "username";
        public static final String AUTHOR_ID = "author_id";
    }

    public static final class Fields {
        public static final String ID = "id";
        public static final String TITLE = "title";
        public static final String TEXT = "text";
        public static final String CREATION_TIME = "creationTime";
        public static final String UPDATE_TIME = "updateTime";
        public static final String USERNAME = "username";
        public static final String AUTHOR_ID = "authorId";
    }
}
package io.github.sakujj.nms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Table
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "news_id")
public class NewsId {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "author_id",  nullable = false, updatable = false)
    private UUID authorId;
}

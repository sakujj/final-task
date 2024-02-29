package io.github.sakujj.nms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class NewsResponse {

    private UUID id;

    private String title;

    private String text;

    private LocalDateTime creationTime;

    private LocalDateTime updateTime;

    private String username;

    private UUID authorId;
}

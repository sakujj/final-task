package io.github.sakujj.nms.util;

import io.github.sakujj.nms.dto.NewsRequest;
import io.github.sakujj.nms.dto.NewsResponse;
import io.github.sakujj.nms.entity.News;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

@With
@Getter
@NoArgsConstructor(staticName = "anArticle")
@AllArgsConstructor
public class NewsTestBuilder {

    private UUID id = UUID.fromString("caf87159-8f25-4e5b-9eb1-39273f6016a6");

    private String title = "The peculiar incident";

    private String text = "A man drove his car off the bridge right into the river and sailed away.";

    private LocalDateTime creationTime = LocalDateTime.of(2024, Month.FEBRUARY, 3, 15, 30);

    private LocalDateTime updateTime = LocalDateTime.of(2024, Month.FEBRUARY, 10, 13, 34);

    private String username = "Famous Journalist";

    private UUID authorId = UUID.fromString("dae860ff-d529-4272-95f4-bf3708c88497");

    public News build() {
        return new News(id, title, text, creationTime, updateTime, username, authorId);
    }

    public NewsResponse buildResponse() {
        return new NewsResponse(id, title, text, creationTime, updateTime, username, authorId);
    }

    public NewsRequest buildRequest() {
        return new NewsRequest(title, text);
    }
}

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@With
@Getter
@AllArgsConstructor
@NoArgsConstructor(staticName = "anArticle")
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

    public static List<NewsTestBuilder> getTestList() {

        NewsTestBuilder firstNews = NewsTestBuilder.anArticle();

        NewsTestBuilder secondNews = NewsTestBuilder.anArticle()
                .withId(UUID.fromString("8de38174-3738-4f88-aac9-28162d48e703"))
                .withText("Extra news")
                .withTitle("Second title")
                .withUsername("Username 2")
                .withAuthorId(UUID.fromString("141da7cb-8142-4151-b199-4c8ce044ff40"))
                .withCreationTime(LocalDateTime.of(2023, Month.MARCH, 3, 14, 14))
                .withUpdateTime(LocalDateTime.of(2024, Month.MARCH, 3, 16, 16));

        NewsTestBuilder thirdNews = NewsTestBuilder.anArticle()
                .withId(UUID.fromString("2e5463dd-8633-4445-a65b-c9d9bb370369"))
                .withText("Extra super bonus news")
                .withTitle("Third title")
                .withUsername("Username 333 xxx ")
                .withAuthorId(UUID.fromString("ca0abe71-a336-48c8-9c4c-6418526e1374"))
                .withCreationTime(LocalDateTime.of(2022, Month.MARCH, 2, 9, 34))
                .withUpdateTime(LocalDateTime.of(2023, Month.MARCH, 4, 19, 37));

        return new ArrayList<>(List.of(firstNews, secondNews, thirdNews));
    }
}

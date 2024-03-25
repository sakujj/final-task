package io.github.sakujj.nms.util;

import io.github.sakujj.nms.dto.CommentResponse;
import io.github.sakujj.nms.dto.CommentSaveRequest;
import io.github.sakujj.nms.dto.CommentUpdateRequest;
import io.github.sakujj.nms.entity.Comment;
import io.github.sakujj.nms.entity.NewsId;
import jakarta.persistence.Column;
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
@NoArgsConstructor(staticName = "aComment")
public class CommentTestBuilder {

    private UUID id = UUID.fromString("c7403bfc-24bc-4086-b764-78b06fd4bd72");

    private String text = "I HAVE TO leave my personal opinion: these news are unbelievable.";

    private LocalDateTime creationTime = LocalDateTime.of(2024, Month.MARCH, 4, 11, 12);

    private LocalDateTime updateTime = LocalDateTime.of(2024, Month.MARCH, 5, 9, 33);

    private String username = "username_number_one";

    private NewsId newsId = null;

    private UUID newsIdUUID = UUID.fromString("bb3a8f23-ce3e-42b3-afcf-a23f22770aa1");

    private UUID authorId = UUID.fromString("65f0a2bb-ac5c-4200-82f8-f90e1cab44d9");

    public Comment build() {
        return new Comment(id, text, creationTime, updateTime, username, newsId, authorId);
    }

    public CommentSaveRequest buildSaveRequest() {
        return new CommentSaveRequest(text, newsIdUUID);
    }

    public CommentUpdateRequest buildUpdateRequest() {
        return new CommentUpdateRequest(text);
    }

    public CommentResponse buildResponse() {
        return new CommentResponse(id, text,creationTime, updateTime, username, newsIdUUID, authorId);
    }

    public static List<CommentTestBuilder> getTestList() {

        CommentTestBuilder firstComment = CommentTestBuilder.aComment();

        CommentTestBuilder secondComment = CommentTestBuilder.aComment()
                .withId(UUID.fromString("8de38174-3738-4f88-aac9-28162d48e703"))
                .withText("Extra news")
                .withNewsIdUUID(UUID.fromString("db73a3f0-809a-4164-bc06-428c3f50ae1a"))
                .withUsername("Username 2")
                .withAuthorId(UUID.fromString("141da7cb-8142-4151-b199-4c8ce044ff40"))
                .withCreationTime(LocalDateTime.of(2023, Month.MARCH, 3, 14, 14))
                .withUpdateTime(LocalDateTime.of(2024, Month.MARCH, 3, 16, 16));

        CommentTestBuilder thirdComment = CommentTestBuilder.aComment()
                .withId(UUID.fromString("2e5463dd-8633-4445-a65b-c9d9bb370369"))
                .withText("Extra super bonus news")
                .withNewsIdUUID(UUID.fromString("ceaf09d8-e0d5-4558-b3e2-80fac74253d3"))
                .withUsername("Username 333 xxx ")
                .withAuthorId(UUID.fromString("ca0abe71-a336-48c8-9c4c-6418526e1374"))
                .withCreationTime(LocalDateTime.of(2022, Month.MARCH, 2, 9, 34))
                .withUpdateTime(LocalDateTime.of(2023, Month.MARCH, 4, 19, 37));

        return new ArrayList<>(List.of(firstComment, secondComment, thirdComment));
    }

}

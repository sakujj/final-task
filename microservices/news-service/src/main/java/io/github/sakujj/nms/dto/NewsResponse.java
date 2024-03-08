package io.github.sakujj.nms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.sakujj.cache.IdentifiableByUUID;
import io.github.sakujj.nms.constant.FormatConstants;
import io.github.sakujj.nms.documentation.OpenApiSchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing news send back in response
 */
@Data
@AllArgsConstructor
public class NewsResponse implements IdentifiableByUUID {

    @Schema(example = OpenApiSchema.Examples.NewsDTO.UUID_EXAMPLE)
    private UUID id;

    @Schema(example = OpenApiSchema.Examples.NewsDTO.TITLE_EXAMPLE)
    private String title;

    @Schema(example = OpenApiSchema.Examples.NewsDTO.TEXT_EXAMPLE)
    private String text;

    @Schema(pattern = OpenApiSchema.Patterns.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FormatConstants.DATE_TIME_FORMAT)
    private LocalDateTime creationTime;

    @Schema(pattern = OpenApiSchema.Patterns.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FormatConstants.DATE_TIME_FORMAT)
    private LocalDateTime updateTime;

    @Schema(example = OpenApiSchema.Examples.NewsDTO.USERNAME_EXAMPLE)
    private String username;

    @Schema(example = OpenApiSchema.Examples.NewsDTO.AUTHOR_ID_EXAMPLE)
    private UUID authorId;

    @Override
    public UUID getUuid() {
        return id;
    }
}

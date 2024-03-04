package io.github.sakujj.nms.dto;

import io.github.sakujj.nms.documentation.OpenApiSchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO representing incoming news from request
 */
@Data
@AllArgsConstructor
public class NewsRequest {

    @NotBlank
    @Pattern(regexp = Constraints.TITLE_PATTERN)
    @Schema(pattern = OpenApiSchema.Patterns.NewsDTO.TITLE_PATTERN,
            example = OpenApiSchema.Examples.NewsDTO.TITLE_EXAMPLE)
    private String title;

    @NotBlank
    @Pattern(regexp = Constraints.TEXT_PATTERN)
    @Schema(pattern = OpenApiSchema.Patterns.NewsDTO.TEXT_PATTERN,
            example = OpenApiSchema.Examples.NewsDTO.TEXT_EXAMPLE)
    private String text;
}

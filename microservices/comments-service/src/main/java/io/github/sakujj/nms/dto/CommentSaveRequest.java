package io.github.sakujj.nms.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentSaveRequest {

    @Pattern(regexp = Contraints.TEXT_PATTERN)
    @Length(min = Contraints.TEXT_MIN_LENGTH, max = Contraints.TEXT_MAX_LENGTH)
    private String text;

    private UUID newsId;
}

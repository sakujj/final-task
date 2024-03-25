package io.github.sakujj.nms.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * An API error returned from {@link RestExceptionHandler}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private String errorMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "The field is present only if something is invalid.", nullable = true)
    private List<String> validationErrors;
}

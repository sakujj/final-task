package io.github.sakujj.nms.dto;

import lombok.experimental.UtilityClass;

/**
 * Constraints for DTO classes
 */
@UtilityClass
public class Constraints {
    public static final String TITLE_PATTERN = ".{5,255}";

    public static final String TEXT_PATTERN = ".{20,200000}";
}

package io.github.sakujj.nms.dto;

import lombok.experimental.UtilityClass;

/**
 * Constraints for DTO classes
 */
@UtilityClass
public class Constraints {
    public static final String TITLE_PATTERN = "\\S+(.*\\S+)?";
    public static final int TITLE_MAX_LENGTH = 255;
    public static final int TITLE_MIN_LENGTH = 1;

    public static final String TEXT_PATTERN = "\\S+(.*\\S+)?";
    public static final int TEXT_MAX_LENGTH = 200_000;
    public static final int TEXT_MIN_LENGTH = 1;
}

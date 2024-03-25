package io.github.sakujj.nms.dto;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Contraints {

    public static final String TEXT_PATTERN = "\\S+(.*\\S+)?";
    public static final int TEXT_MAX_LENGTH = 5_000;
    public static final int TEXT_MIN_LENGTH = 1;

}

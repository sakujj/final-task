package io.github.sakujj.nms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewsRequest {

    private String title;

    private String text;
}

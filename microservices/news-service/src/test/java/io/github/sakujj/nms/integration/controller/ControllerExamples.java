package io.github.sakujj.nms.integration.controller;

import io.github.sakujj.nms.constant.FormatConstants;
import io.github.sakujj.nms.dto.NewsRequest;
import io.github.sakujj.nms.dto.NewsResponse;
import io.github.sakujj.nms.entity.News;
import io.github.sakujj.nms.util.NewsTestBuilder;
import lombok.experimental.UtilityClass;

import java.text.MessageFormat;

@UtilityClass
public class ControllerExamples {
    public static class WellFormed {

        public final static NewsRequest NEWS_REQUEST_JAVA = NewsTestBuilder.anArticle().buildRequest();

        public final static String NEWS_REQUEST_JSON = MessageFormat.format("""
                        '{'
                            "{0}":"{1}",
                            "{2}":"{3}"
                        '}'
                        """,
                News.Fields.TEXT, NEWS_REQUEST_JAVA.getText(),
                News.Fields.TITLE, NEWS_REQUEST_JAVA.getTitle()
        );

        public final static NewsResponse NEWS_RESPONSE_JAVA = NewsTestBuilder.anArticle().buildResponse();

        public final static String NEWS_RESPONSE_JSON = MessageFormat.format("""
                        '{'
                            "{0}":"{1}",
                            "{2}":"{3}",
                            "{4}":"{5}",
                            "{6}":"{7}",
                            "{8}":"{9}",
                            "{10}":"{11}",
                            "{12}":"{13}"
                        '}'
                        """,
                News.Fields.TEXT, NEWS_RESPONSE_JAVA.getText(), // 0
                News.Fields.TITLE, NEWS_RESPONSE_JAVA.getTitle(), // 2
                News.Fields.ID, NEWS_RESPONSE_JAVA.getId(), // 4
                News.Fields.AUTHOR_ID, NEWS_RESPONSE_JAVA.getAuthorId(), // 6
                News.Fields.CREATION_TIME, FormatConstants.DATE_TIME_FORMATTER.format(NEWS_RESPONSE_JAVA.getCreationTime()), // 8
                News.Fields.UPDATE_TIME, FormatConstants.DATE_TIME_FORMATTER.format(NEWS_RESPONSE_JAVA.getUpdateTime()), // 10
                News.Fields.USERNAME, NEWS_RESPONSE_JAVA.getUsername() // 12
        );
    }
}

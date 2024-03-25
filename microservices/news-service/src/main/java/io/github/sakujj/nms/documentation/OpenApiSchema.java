package io.github.sakujj.nms.documentation;

import io.github.sakujj.nms.constant.FormatConstants;
import io.github.sakujj.nms.dto.Constraints;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.experimental.UtilityClass;

/**
 * Contains data for OpenAPI schema and sets OpenApiDefinition
 */
@UtilityClass
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Pavel Rysnik",
                        url = "https://github.com/sakujj",
                        email = "pavelrysnik@gmail.com"
                ),
                description = "News REST-service documentation",
                title = "OpenApi | News REST-service documentation",
                version = "0.1"
        )
)
public class OpenApiSchema {

    public static class Examples {

        public static class NewsDTO {

            public static final String UUID_EXAMPLE = "acb8316d-3d13-4096-b1d6-f997b7307f0e";
            public static final String TITLE_EXAMPLE = "TITLE_1";
            public static final String TEXT_EXAMPLE = "SOME INFO НЕКОТОРАЯ ИНФОРМАЦИЯ";
            public static final String USERNAME_EXAMPLE = "ПОЛЬЗОВАТЕЛЬ X";
            public static final String AUTHOR_ID_EXAMPLE = "7dc65e29-fa77-4be7-89db-a8a49d930266";
        }
    }

    public static class Patterns {

        public static final String DATE_TIME_FORMAT = FormatConstants.DATE_TIME_FORMAT;

        public static class NewsDTO {

            public static final String TITLE_PATTERN = Constraints.TITLE_PATTERN;
            public static final String TEXT_PATTERN = Constraints.TEXT_PATTERN;
        }
    }

}


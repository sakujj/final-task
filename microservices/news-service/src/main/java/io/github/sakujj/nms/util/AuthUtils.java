package io.github.sakujj.nms.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@UtilityClass
public class AuthUtils {

    public static String getBearerAuthHeaderValue(JwtAuthenticationToken token) {
        return "Bearer " + token.getToken().getTokenValue();
    }

    public static String getBearerAuthHeaderValue(String tokenValue) {
        return "Bearer " + tokenValue;
    }
}

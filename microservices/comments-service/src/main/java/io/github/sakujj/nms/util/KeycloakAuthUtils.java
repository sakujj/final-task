package io.github.sakujj.nms.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

/**
 * Keycloak authentication and authorization utils.
 */
@UtilityClass
public class KeycloakAuthUtils {

    public static String getUsernameOfAuthenticatedUserKeycloak(JwtAuthenticationToken idToken) {
        Map<String, Object> claims = idToken.getTokenAttributes();
        return (String) claims.get("preferred_username");
    }
}

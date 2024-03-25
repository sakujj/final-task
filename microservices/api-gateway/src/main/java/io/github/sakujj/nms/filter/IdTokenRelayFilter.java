package io.github.sakujj.nms.filter;

import lombok.experimental.UtilityClass;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.security.Principal;

/**
 * Inspired by {@link org.springframework.cloud.gateway.server.mvc.filter.TokenRelayFilterFunctions#tokenRelay()}
 */
@UtilityClass
public class IdTokenRelayFilter {

    public static HandlerFilterFunction<ServerResponse, ServerResponse> idTokenRelay() {
        return (request, next) -> {
            Principal requestPrincipal = request.servletRequest().getUserPrincipal();

            if (!(requestPrincipal instanceof OAuth2AuthenticationToken)) {
                return next.handle(request);
            }
            var auth = (OAuth2AuthenticationToken) requestPrincipal;

            OAuth2User authTokenPrincipal = auth.getPrincipal();
            if (!(authTokenPrincipal instanceof OidcUser)) {
                return next.handle(request);
            }
            var oidcUser = (OidcUser) authTokenPrincipal;

            ServerRequest modifiedRequest = ServerRequest.from(request)
                    .headers(httpHeaders -> httpHeaders
                            .setBearerAuth(oidcUser.getIdToken().getTokenValue()))
                    .build();

            return next.handle(modifiedRequest);
        };
    }
}

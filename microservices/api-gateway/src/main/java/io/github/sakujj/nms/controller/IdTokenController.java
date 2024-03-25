package io.github.sakujj.nms.controller;

import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller to get your jwt id token, issued by an authorization server
 */
@RestController
public class IdTokenController {

    @GetMapping("/get-token")
    public String getIdToken(OAuth2AuthenticationToken auth2AuthenticationToken) {

        return ((OidcUser) auth2AuthenticationToken.getPrincipal())
                .getIdToken()
                .getTokenValue();
    }
}

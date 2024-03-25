package io.github.sakujj.nms.security;

import io.github.sakujj.nms.constant.RoleConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.NullSecurityContextRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChainResourceServer(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .securityMatcher("/**")
                .sessionManagement(
                        configurer -> configurer
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> {
                            auth.requestMatchers("comments/news-ids/**")
                                    .hasAuthority(RoleConstants.ADMIN);
                            auth.anyRequest().permitAll();
                        }
                )
                .oauth2ResourceServer(
                        resourceServerConfigurer -> {
                            var authoritiesConverter = new JwtKeycloakRealmAccessGrantedAuthoritiesConverter();

                            var authenticationConverter = new JwtAuthenticationConverter();
                            authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

                            resourceServerConfigurer
                                    .jwt(jwtConfigurer -> jwtConfigurer
                                            .jwtAuthenticationConverter(authenticationConverter));
                        }
                )
                .securityContext(s -> s.securityContextRepository(new NullSecurityContextRepository()))
                .build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain securityFilterChainAuthServer(HttpSecurity httpSecurity, OidcClientInitiatedLogoutSuccessHandler logoutSuccessHandler) throws Exception {
        return httpSecurity
                .securityMatchers(customizer -> {
                    customizer.requestMatchers("/login/**");
                    customizer.requestMatchers("/oauth2/**");
                    customizer.requestMatchers("/logout/**");
                    customizer.requestMatchers("/get-token");
                })
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/get-token").authenticated();
                    auth.anyRequest().permitAll();
                })
                .oauth2Login(loginConfigurer -> {
                            loginConfigurer.successHandler((request, response, authentication) ->
                                    request.getRequestDispatcher("/get-token")
                                            .forward(request, response));
                        }
                )
                .logout(logoutConfigurer -> {
                    logoutConfigurer.logoutSuccessHandler(logoutSuccessHandler);
                    logoutConfigurer.invalidateHttpSession(true);
                })
                .build();
    }

    private static class JwtKeycloakRealmAccessGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Map<String, List<String>> realmAccessClaim = jwt.getClaim("realm_access");
            if (realmAccessClaim == null) {
                return null;
            }

            List<String> roles = realmAccessClaim.get("roles");
            if (roles == null) {
                return null;
            }

            return roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast)
                    .toList();
        }
    }

    @Bean
    public OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedLogoutSuccessHandler successHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        successHandler.setPostLogoutRedirectUri("/login");
        return successHandler;
    }
}

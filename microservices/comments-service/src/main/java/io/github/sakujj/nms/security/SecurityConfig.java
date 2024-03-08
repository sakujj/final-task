package io.github.sakujj.nms.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.NullSecurityContextRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Security configuration for the application
 */
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /**
     * OpenAPI configuration to provide Bearer token authorization
     * @return OpenAPI config
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().addSecurityItem(new SecurityRequirement().
                        addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes
                        ("Bearer Authentication", new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .bearerFormat("JWT")
                                .scheme("bearer")));
    }

    /**
     * Main security filter chain
     * @param httpSecurity an http security instance to configure and build SecurityFilterChain
     * @return SecurityFilterChain
     * @throws Exception
     */
    @Bean
    SecurityFilterChain securityFilterChainResourceServer(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .sessionManagement(
                        configurer -> configurer
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> {
                            auth.anyRequest().permitAll();
                        }
                )
                .anonymous(AbstractHttpConfigurer::disable)
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

    /**
     * A roles converter specific to Keycloak jwt tokens, i.e. when roles specified with the "realm_access" claim of a pattern like:
     * <p>&nbsp&nbsp realm_access:</p>
     * <p>&nbsp&nbsp&nbsp&nbsp roles: [</p>
     * <p>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp role_name_1, role_name_2, ...</p>
     * <p>&nbsp&nbsp&nbsp&nbsp ]</p>
     */
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
}

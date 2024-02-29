package io.github.sakujj.nms.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

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
                .securityContext(s-> s.securityContextRepository(new NullSecurityContextRepository()))
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
}

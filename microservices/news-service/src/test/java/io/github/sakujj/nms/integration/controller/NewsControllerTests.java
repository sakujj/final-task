package io.github.sakujj.nms.integration.controller;

import io.github.sakujj.nms.constant.RoleConstants;
import io.github.sakujj.nms.dto.NewsResponse;
import io.github.sakujj.nms.exception.RestExceptionHandler;
import io.github.sakujj.nms.security.SecurityConfig;
import io.github.sakujj.nms.service.NewsService;
import io.github.sakujj.nms.util.NewsTestBuilder;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;
import java.util.Collection;
import java.util.UUID;

import static io.github.sakujj.nms.NewsServiceApplication.NEWS_CONTROLLER_URI;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest
@Import(AopAutoConfiguration.class)
@ContextConfiguration(classes = {MockMvcConfig.class, SecurityConfig.class})
public class NewsControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService;

    @SpyBean
    private RestExceptionHandler restExceptionHandler;

    @Nested
    @DisplayName("PUT " + NEWS_CONTROLLER_URI + "/{id}")
    class putByIdEndpoint {
        @Test
        void shouldEnterMethod_and_serializeAndDeserialize_and_callNewsServiceCorrectly_whenAdminAuthority() throws Exception {

            // given
            String subClaim = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA.getAuthorId().toString();
            String preferredUsernameClaim = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA.getUsername();

            ResponseEntity<NewsResponse> expectedFromService = ResponseEntity.ok(ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA);

            Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(RoleConstants.ADMIN);

            UUID idOfNewsToReplace = NewsTestBuilder.anArticle().getId();

            when(newsService.replace(
                    eq(idOfNewsToReplace),
                    eq(ControllerExamples.WellFormed.NEWS_REQUEST_JAVA),
                    eq(authorities),
                    eq(UUID.fromString(subClaim)),
                    eq(preferredUsernameClaim))
            ).thenReturn(expectedFromService);

            // when, then
            mockMvc.perform(MockMvcRequestBuilders.put(NEWS_CONTROLLER_URI + "/{id}", idOfNewsToReplace).with(jwt()
                                    .authorities(authorities)
                                    .jwt(jwt -> jwt.claims(claims ->
                                    {
                                        claims.put("sub", subClaim);
                                        claims.put("preferred_username", preferredUsernameClaim);
                                    })))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ControllerExamples.WellFormed.NEWS_REQUEST_JSON))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(expectedFromService.getStatusCode().value()))
                    .andExpect(content().json(ControllerExamples.WellFormed.NEWS_RESPONSE_JSON));

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldReturnCreatedStatusAndLocationHeader_and_serializeAndDeserialize_whenServiceTellsThatNewNewsWereCreated()
                throws Exception {

            // given
            String subClaim = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA.getAuthorId().toString();
            String preferredUsernameClaim = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA.getUsername();


            ResponseEntity<NewsResponse> expectedFromService = ResponseEntity
                    .created(URI.create("some/uri"))
                    .body(ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA);

            Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(RoleConstants.ADMIN);

            UUID idOfNewsToReplace = NewsTestBuilder.anArticle().getId();

            when(newsService.replace(
                    eq(idOfNewsToReplace),
                    eq(ControllerExamples.WellFormed.NEWS_REQUEST_JAVA),
                    eq(authorities),
                    eq(UUID.fromString(subClaim)),
                    eq(preferredUsernameClaim))
            ).thenReturn(expectedFromService);

            // when, then
            mockMvc.perform(MockMvcRequestBuilders.put(NEWS_CONTROLLER_URI + "/{id}", idOfNewsToReplace).with(jwt()
                                    .authorities(authorities)
                                    .jwt(jwt -> jwt.claims(claims ->
                                    {
                                        claims.put("sub", subClaim);
                                        claims.put("preferred_username", preferredUsernameClaim);
                                    })))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ControllerExamples.WellFormed.NEWS_REQUEST_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, CoreMatchers.endsWith(NEWS_CONTROLLER_URI + "/" + idOfNewsToReplace)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().json(ControllerExamples.WellFormed.NEWS_RESPONSE_JSON));

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldEnterMethod_and_serializeAndDeserialize_and_callNewsServiceCorrectly_whenJournalistAuthority() throws Exception {

            // given
            String subClaim = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA.getAuthorId().toString();
            String preferredUsernameClaim = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA.getUsername();

            ResponseEntity<NewsResponse> expectedFromService = ResponseEntity.ok(ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA);

            Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(RoleConstants.JOURNALIST);

            UUID idOfNewsToReplace = NewsTestBuilder.anArticle().getId();

            when(newsService.replace(
                    eq(idOfNewsToReplace),
                    eq(ControllerExamples.WellFormed.NEWS_REQUEST_JAVA),
                    eq(authorities),
                    eq(UUID.fromString(subClaim)),
                    eq(preferredUsernameClaim))
            ).thenReturn(expectedFromService);

            // when, then
            mockMvc.perform(MockMvcRequestBuilders.put(NEWS_CONTROLLER_URI + "/{id}", idOfNewsToReplace).with(jwt()
                                    .authorities(authorities)
                                    .jwt(jwt -> jwt.claims(claims ->
                                    {
                                        claims.put("sub", subClaim);
                                        claims.put("preferred_username", preferredUsernameClaim);
                                    })))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ControllerExamples.WellFormed.NEWS_REQUEST_JSON))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(expectedFromService.getStatusCode().value()))
                    .andExpect(content().json(ControllerExamples.WellFormed.NEWS_RESPONSE_JSON));

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldNotEnterMethod_whenSubscriberAuthority() throws Exception {

            // given
            String subClaim = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA.getAuthorId().toString();
            String preferredUsernameClaim = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA.getUsername();


            Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(RoleConstants.SUBSCRIBER);

            UUID idOfNewsToReplace = NewsTestBuilder.anArticle().getId();

            // when, then
            mockMvc.perform(MockMvcRequestBuilders.put(NEWS_CONTROLLER_URI + "/{id}", idOfNewsToReplace).with(jwt()
                                    .authorities(authorities)
                                    .jwt(jwt -> jwt.claims(claims ->
                                    {
                                        claims.put("sub", subClaim);
                                        claims.put("preferred_username", preferredUsernameClaim);
                                    })))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ControllerExamples.WellFormed.NEWS_REQUEST_JSON))
                    .andExpect(status().isForbidden());

            verify(restExceptionHandler).handleAccessDeniedException(any(AccessDeniedException.class));
        }

        @Test
        void shouldNotEnterMethod_whenNoAuthentication() throws Exception {

            // given
            UUID idOfNewsToReplace = NewsTestBuilder.anArticle().getId();

            // when, then
            mockMvc.perform(MockMvcRequestBuilders.put(NEWS_CONTROLLER_URI + "/{id}", idOfNewsToReplace)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ControllerExamples.WellFormed.NEWS_REQUEST_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$", Matchers.hasItems()));

            verify(restExceptionHandler).handleAuthenticationException(any(AuthenticationException.class));
        }

    }

}

package io.github.sakujj.nms.integration.controller;

import io.github.sakujj.nms.NewsServiceApplication;
import io.github.sakujj.nms.constant.RoleConstants;
import io.github.sakujj.nms.dto.NewsRequest;
import io.github.sakujj.nms.dto.NewsResponse;
import io.github.sakujj.nms.exception.RestExceptionHandler;
import io.github.sakujj.nms.integration.testcontainer.CommonPostgresContainerInitializer;
import io.github.sakujj.nms.security.SecurityConfig;
import io.github.sakujj.nms.service.NewsService;
import io.github.sakujj.nms.service.NewsServiceImpl;
import io.github.sakujj.nms.util.NewsTestBuilder;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.github.sakujj.nms.NewsServiceApplication.NEWS_CONTROLLER_URI;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AopAutoConfiguration.class)
@ContextConfiguration(classes = {MockMvcConfig.class, SecurityConfig.class, NewsServiceApplication.class})
public class NewsControllerTests extends CommonPostgresContainerInitializer {

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
        void shouldThrowAccessDenied_ifNewsNotFoundInService_andAuthedUserIsJournalistButNotAdmin() throws Exception {

            // given
            String subClaim = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA.getAuthorId().toString();
            String preferredUsernameClaim = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA.getUsername();
            Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(RoleConstants.JOURNALIST);

            UUID idOfNewsToUpdate = NewsTestBuilder.anArticle().getId();

            Mockito.when(newsService.findById(idOfNewsToUpdate))
                    .thenReturn(Optional.empty());

            // when, then
            mockMvc.perform(MockMvcRequestBuilders.put(NEWS_CONTROLLER_URI + "/{id}", idOfNewsToUpdate).with(jwt()
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
        void shouldRespondStatusNotFound_ifNewsNotFoundInService_andAuthedUserIsAdmin() throws Exception {

            // given
            String subClaim = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA.getAuthorId().toString();
            String preferredUsernameClaim = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA.getUsername();
            Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(RoleConstants.ADMIN);

            UUID idOfNewsToUpdate = NewsTestBuilder.anArticle().getId();

            Mockito.when(newsService.findById(idOfNewsToUpdate))
                    .thenReturn(Optional.empty());

            // when, then
            mockMvc.perform(MockMvcRequestBuilders.put(NEWS_CONTROLLER_URI + "/{id}", idOfNewsToUpdate).with(jwt()
                                    .authorities(authorities)
                                    .jwt(jwt -> jwt.claims(claims ->
                                    {
                                        claims.put("sub", subClaim);
                                        claims.put("preferred_username", preferredUsernameClaim);
                                    })))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ControllerExamples.WellFormed.NEWS_REQUEST_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(Matchers.blankOrNullString()));

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldThrowAccessDenied_ifAuthorIdIsNotAuthedUserId_andAuthedUserIsNotAdmin() throws Exception {

            // given
            String subClaim = "e24ea5c9-6570-4d94-ab63-7e612b651915";
            String preferredUsernameClaim = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA.getUsername();
            Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(RoleConstants.JOURNALIST);

            UUID idOfNewsToUpdate = NewsTestBuilder.anArticle().getId();

            NewsResponse responseFromService = NewsTestBuilder.anArticle()
                    .withAuthorId(UUID.fromString("08eb8d0b-53b5-4885-81ba-d950c50dc5b5"))
                    .buildResponse();

            Mockito.when(newsService.findById(idOfNewsToUpdate))
                    .thenReturn(Optional.of(responseFromService));

            // when, then
            mockMvc.perform(MockMvcRequestBuilders.put(NEWS_CONTROLLER_URI + "/{id}", idOfNewsToUpdate).with(jwt()
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
        void shouldUpdate_ifAuthorIdIsNotAuthedUserId_butAuthedUserIsAdmin() throws Exception {

            // given
            UUID idOfNewsToUpdate = NewsTestBuilder.anArticle().getId();

            NewsRequest requestToUpdate = ControllerExamples.WellFormed.NEWS_REQUEST_JAVA;
            NewsResponse expectedResponse = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA;

            String requestToUpdateJson = ControllerExamples.WellFormed.NEWS_REQUEST_JSON;
            String expectedResponseJson = ControllerExamples.WellFormed.NEWS_RESPONSE_JSON;

            String subClaimVal = "e24ea5c9-6570-4d94-ab63-7e612b651915";
            String preferredUsernameClaimVal = expectedResponse.getUsername();
            Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(RoleConstants.ADMIN);

            NewsResponse responseFromService = NewsTestBuilder.anArticle()
                    .withAuthorId(UUID.fromString("08eb8d0b-53b5-4885-81ba-d950c50dc5b5"))
                    .buildResponse();

            Mockito.when(newsService.findById(idOfNewsToUpdate))
                    .thenReturn(Optional.of(responseFromService));
            Mockito.when(newsService.update(idOfNewsToUpdate, requestToUpdate))
                            .thenReturn(Optional.of(expectedResponse));

            // when, then
            mockMvc.perform(MockMvcRequestBuilders.put(NEWS_CONTROLLER_URI + "/{id}", idOfNewsToUpdate).with(jwt()
                                    .authorities(authorities)
                                    .jwt(jwt -> jwt.claims(claims ->
                                    {
                                        claims.put("sub", subClaimVal);
                                        claims.put("preferred_username", preferredUsernameClaimVal);
                                    })))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestToUpdateJson))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponseJson));

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldUpdate_ifAuthorIdIsAuthedUserId_andAuthedUserIsJournalist() throws Exception {

            // given

            UUID idOfNewsToUpdate = NewsTestBuilder.anArticle().getId();

            NewsRequest requestToUpdate = ControllerExamples.WellFormed.NEWS_REQUEST_JAVA;
            NewsResponse expectedResponse = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA;

            String requestToUpdateJson = ControllerExamples.WellFormed.NEWS_REQUEST_JSON;
            String expectedResponseJson = ControllerExamples.WellFormed.NEWS_RESPONSE_JSON;

            // sub claim val is authed user id
            String subClaimVal = "e24ea5c9-6570-4d94-ab63-7e612b651915";
            String preferredUsernameClaimVal = expectedResponse.getUsername();
            Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(RoleConstants.JOURNALIST);

            NewsResponse responseFromService = NewsTestBuilder.anArticle()
                    .withAuthorId(UUID.fromString(subClaimVal))
                    .buildResponse();

            Mockito.when(newsService.findById(idOfNewsToUpdate))
                    .thenReturn(Optional.of(responseFromService));
            Mockito.when(newsService.update(idOfNewsToUpdate, requestToUpdate))
                    .thenReturn(Optional.of(expectedResponse));

            // when, then
            mockMvc.perform(MockMvcRequestBuilders.put(NEWS_CONTROLLER_URI + "/{id}", idOfNewsToUpdate).with(jwt()
                                    .authorities(authorities)
                                    .jwt(jwt -> jwt.claims(claims ->
                                    {
                                        claims.put("sub", subClaimVal);
                                        claims.put("preferred_username", preferredUsernameClaimVal);
                                    })))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestToUpdateJson))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponseJson));

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldRespondStatusUnauthorized_ifUnauthenticated() throws Exception {

            // given
            UUID idOfNewsToUpdate = NewsTestBuilder.anArticle().getId();

            String requestToUpdateJson = ControllerExamples.WellFormed.NEWS_REQUEST_JSON;

            // when, then
            mockMvc.perform(MockMvcRequestBuilders.put(NEWS_CONTROLLER_URI + "/{id}", idOfNewsToUpdate)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestToUpdateJson))
                    .andExpect(status().isUnauthorized());

            verify(restExceptionHandler).handleAuthenticationException(any(AuthenticationException.class));
        }

        @Test
        void shouldRespondStatusForbidden_ifAuthedUserIsSubscriber() throws Exception {

            // given

            String subClaimVal = "e24ea5c9-6570-4d94-ab63-7e612b651915";
            String preferredUsernameClaimVal = ControllerExamples.WellFormed.NEWS_RESPONSE_JAVA.getUsername();
            Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(RoleConstants.SUBSCRIBER);

            UUID idOfNewsToUpdate = NewsTestBuilder.anArticle().getId();

            String requestToUpdateJson = ControllerExamples.WellFormed.NEWS_REQUEST_JSON;

            // when, then
            mockMvc.perform(MockMvcRequestBuilders.put(NEWS_CONTROLLER_URI + "/{id}", idOfNewsToUpdate).with(jwt()
                                    .authorities(authorities)
                                    .jwt(jwt -> jwt.claims(claims ->
                                    {
                                        claims.put("sub", subClaimVal);
                                        claims.put("preferred_username", preferredUsernameClaimVal);
                                    })))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestToUpdateJson))
                    .andExpect(status().isForbidden());

            verify(restExceptionHandler).handleAccessDeniedException(any(AccessDeniedException.class));
        }

    }
}

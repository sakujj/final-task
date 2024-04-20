package io.github.sakujj.nms.controller;

import io.github.sakujj.nms.constant.RoleConstants;
import io.github.sakujj.nms.dto.NewsRequest;
import io.github.sakujj.nms.dto.NewsResponse;
import io.github.sakujj.nms.external.dto.CommentResponse;
import io.github.sakujj.nms.external.dto.CommentSaveRequest;
import io.github.sakujj.nms.external.dto.CommentUpdateRequest;
import io.github.sakujj.nms.httpclient.CommentsClient;
import io.github.sakujj.nms.service.NewsService;
import io.github.sakujj.nms.util.AuthUtils;
import io.github.sakujj.nms.util.KeycloakAuthUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.github.sakujj.nms.NewsServiceApplication.NEWS_CONTROLLER_URI;

/**
 * Controller for News-microservice.
 * Has dedicated OpenAPI specification at {@link NewsControllerSpec}
 */
@Validated
@RestController
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
@RequestMapping(value = NEWS_CONTROLLER_URI, produces = MediaType.APPLICATION_JSON_VALUE)
public class NewsController implements NewsControllerSpec {

    public final static String PAGE_NUMBER_PARAM_NAME = "page";
    public final static String PAGE_NUMBER_DEFAULT_VAL = "0";

    public final static String PAGE_SIZE_PARAM_NAME = "size";
    public final static String PAGE_SIZE_DEFAULT_VAL = "10";

    public final static String CONTAINED_IN_USERNAME_PARAM_NAME = "author-name-having";
    public final static String CONTAINED_IN_TITLE_PARAM_NAME = "title-having";


    private final NewsService newsService;

    private final CommentsClient commentsClient;

    @GetMapping("/{newsId}/comments")
    public ResponseEntity<Page<CommentResponse>> findAllComments(
            @PathVariable("newsId") UUID newsId,

            @RequestParam(value = PAGE_NUMBER_PARAM_NAME, defaultValue = PAGE_NUMBER_DEFAULT_VAL)
            Integer pageNumber,

            @RequestParam(value = PAGE_SIZE_PARAM_NAME, defaultValue = PAGE_SIZE_DEFAULT_VAL)
            Integer pageSize,

            @RequestParam(required = false, value = CONTAINED_IN_USERNAME_PARAM_NAME)
            String containedInUsername) {

        return commentsClient.findAll(pageNumber, pageSize, containedInUsername, newsId);
    }

    @GetMapping("/{newsId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> findComment(
            @PathVariable("newsId") UUID newsId,
            @PathVariable("commentId") UUID commentId) {

        ResponseEntity<CommentResponse> foundById = commentsClient.findById(commentId);
        if (foundById.getStatusCode().is4xxClientError()) {
            return foundById;
        }

        CommentResponse commentResponse = foundById.getBody();
        if (commentResponse == null
                || !commentResponse.getNewsId().equals(newsId)) {

            return ResponseEntity.notFound().build();
        }

        return foundById;
    }

    @PostMapping("/{newsId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable("newsId") UUID newsId,
            @RequestBody CommentUpdateRequest commentUpdateRequest,
            JwtAuthenticationToken idToken) {

        CommentSaveRequest commentSaveRequest = new CommentSaveRequest(
                commentUpdateRequest.getText(),
                newsId);

        return commentsClient.create(commentSaveRequest, AuthUtils.getBearerAuthHeaderValue(idToken));
    }

    @DeleteMapping("/{newsId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("newsId") UUID newsId,
            @PathVariable("commentId") UUID commentId,
            JwtAuthenticationToken idToken) {

        return commentsClient.delete(commentId, AuthUtils.getBearerAuthHeaderValue(idToken));
    }

    @PutMapping("/{newsId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable("newsId") UUID newsId,
            @PathVariable("commentId") UUID commentId,
            @RequestBody CommentUpdateRequest commentUpdateRequest,
            JwtAuthenticationToken idToken) {

        return commentsClient.update(commentUpdateRequest, commentId, AuthUtils.getBearerAuthHeaderValue(idToken));
    }

    @GetMapping(params = {CONTAINED_IN_USERNAME_PARAM_NAME, CONTAINED_IN_TITLE_PARAM_NAME})
    public ResponseEntity<Page<NewsResponse>> findAllWithContainedInUsernameAndContainedInTitle(
            @Min(MIN_PAGE_NUMBER)
            @RequestParam(value = PAGE_NUMBER_PARAM_NAME, defaultValue = PAGE_NUMBER_DEFAULT_VAL)
            Integer pageNumber,

            @Min(MIN_PAGE_SIZE)
            @Max(MAX_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAM_NAME, defaultValue = PAGE_SIZE_DEFAULT_VAL)
            Integer pageSize,

            @RequestParam(CONTAINED_IN_USERNAME_PARAM_NAME)
            String containedInUsername,

            @RequestParam(CONTAINED_IN_TITLE_PARAM_NAME)
            String containedInTitle) {

                    Page<NewsResponse> pageFound = newsService.findByTitleContainingAndUsernameContaining(
                    containedInTitle,
                    containedInUsername,
                    pageNumber,
                    pageSize);

            return getFoundPageResponseEntity(pageFound);
    }

    @GetMapping(params = {CONTAINED_IN_USERNAME_PARAM_NAME, "!" + CONTAINED_IN_TITLE_PARAM_NAME})
    public ResponseEntity<Page<NewsResponse>> findAllWithContainedInUsername(
            @Min(MIN_PAGE_NUMBER)
            @RequestParam(value = PAGE_NUMBER_PARAM_NAME, defaultValue = PAGE_NUMBER_DEFAULT_VAL)
            Integer pageNumber,

            @Min(MIN_PAGE_SIZE)
            @Max(MAX_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAM_NAME, defaultValue = PAGE_SIZE_DEFAULT_VAL)
            Integer pageSize,

            @RequestParam(CONTAINED_IN_USERNAME_PARAM_NAME)
            String containedInUsername) {

        Page<NewsResponse> pageFound = newsService.findByUsernameContaining(
                containedInUsername,
                pageNumber,
                pageSize);

        return getFoundPageResponseEntity(pageFound);
    }

    @GetMapping(params = {"!" + CONTAINED_IN_USERNAME_PARAM_NAME, CONTAINED_IN_TITLE_PARAM_NAME})
    public ResponseEntity<Page<NewsResponse>> findAllWithContainedInTitle(
            @Min(MIN_PAGE_NUMBER)
            @RequestParam(value = PAGE_NUMBER_PARAM_NAME, defaultValue = PAGE_NUMBER_DEFAULT_VAL)
            Integer pageNumber,

            @Min(MIN_PAGE_SIZE)
            @Max(MAX_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAM_NAME, defaultValue = PAGE_SIZE_DEFAULT_VAL)
            Integer pageSize,

            @RequestParam(CONTAINED_IN_TITLE_PARAM_NAME)
            String containedInTitle) {

        Page<NewsResponse> pageFound = newsService.findByTitleContaining(
                containedInTitle,
                pageNumber,
                pageSize);

        return getFoundPageResponseEntity(pageFound);
    }



    @GetMapping(params = {"!" + CONTAINED_IN_USERNAME_PARAM_NAME, "!" + CONTAINED_IN_TITLE_PARAM_NAME})
    public ResponseEntity<Page<NewsResponse>> findAll(
            @Min(MIN_PAGE_NUMBER)
            @RequestParam(value = PAGE_NUMBER_PARAM_NAME, defaultValue = PAGE_NUMBER_DEFAULT_VAL)
            Integer pageNumber,

            @Min(MIN_PAGE_SIZE)
            @Max(MAX_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAM_NAME, defaultValue = PAGE_SIZE_DEFAULT_VAL)
            Integer pageSize) {

        Page<NewsResponse> pageFound = newsService.findAll(pageNumber, pageSize);

        return getFoundPageResponseEntity(pageFound);
    }

    private static <T> ResponseEntity<Page<T>> getFoundPageResponseEntity(Page<T> pageFound) {
        if (pageFound.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(pageFound);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsResponse> findById(@PathVariable("id") UUID id) {

        Optional<NewsResponse> newsResponseOptional = newsService.findById(id);

        return newsResponseOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID newsId, JwtAuthenticationToken idToken) {

        Collection<GrantedAuthority> authoritiesOfAuthenticatedUser = idToken.getAuthorities();

        boolean isAuthenticatedUserAdminOrJournalist = authoritiesOfAuthenticatedUser.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> switch (authority) {
                    case RoleConstants.ADMIN, RoleConstants.JOURNALIST -> true;
                    default -> false;
                });

        if (!isAuthenticatedUserAdminOrJournalist) {

            return ResponseEntity.noContent().build();
        }

        Optional<NewsResponse> newsResponseOptional = newsService.findById(newsId);
        if (newsResponseOptional.isEmpty()) {

            return ResponseEntity.noContent().build();
        }

        NewsResponse newsResponse = newsResponseOptional.get();
        UUID idOfNewsAuthor = newsResponse.getAuthorId();

        UUID idOfAuthenticatedUser = UUID.fromString(idToken.getName());

        if (!isAuthenticatedUserAuthorizedToChangeNews(authoritiesOfAuthenticatedUser, idOfAuthenticatedUser, idOfNewsAuthor)) {

            return ResponseEntity.noContent().build();
        }

        newsService.deleteById(newsId);
        commentsClient.deleteNewsId(newsId, AuthUtils.getBearerAuthHeaderValue(idToken));

        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @Secured({RoleConstants.ADMIN, RoleConstants.JOURNALIST})
    public ResponseEntity<NewsResponse> create(@RequestBody @Valid NewsRequest newsRequest,
                                               JwtAuthenticationToken idToken) {

        UUID authenticatedUserId = UUID.fromString(idToken.getName());
        String authenticatedUserUsername = KeycloakAuthUtils.getUsernameOfAuthenticatedUserKeycloak(idToken);

        NewsResponse newsResponse = newsService.create(newsRequest, authenticatedUserId, authenticatedUserUsername);
        commentsClient.createNewsId(newsResponse.getId(), AuthUtils.getBearerAuthHeaderValue(idToken));

        return ResponseEntity.created(
                        ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(newsResponse.getId())
                                .toUri())
                .body(newsResponse);
    }

    @PutMapping("/{id}")
    @Secured({RoleConstants.ADMIN, RoleConstants.JOURNALIST})
    public ResponseEntity<NewsResponse> update(@RequestBody @Valid NewsRequest newsRequest,
                                               @PathVariable("id") UUID newsId,
                                               JwtAuthenticationToken idToken) {

        Optional<NewsResponse> newsOptional = newsService.findById(newsId);
        if (newsOptional.isEmpty()) {

            if (!idTokenContainsAuthority(idToken, RoleConstants.ADMIN)) {
                throw new AccessDeniedException("You do not have authority to update the specified news");
            }

            return ResponseEntity.notFound().build();
        }

        NewsResponse newsToUpdate = newsOptional.get();

        UUID idOfAuthor = newsToUpdate.getAuthorId();
        UUID idOfAuthenticatedUser = UUID.fromString(idToken.getName());

        if (!idTokenContainsAuthority(idToken, RoleConstants.ADMIN)
                && idTokenContainsAuthority(idToken, RoleConstants.JOURNALIST)) {

            if (!idOfAuthenticatedUser.equals(idOfAuthor)) {
                throw new AccessDeniedException("You do not have authority to update the specified news");
            }
        }

        return newsService.update(newsId, newsRequest)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static boolean idTokenContainsAuthority(JwtAuthenticationToken idToken, String authority) {

        Collection<GrantedAuthority> authoritiesOfAuthenticatedUser = idToken.getAuthorities();

        return authoritiesOfAuthenticatedUser.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(authority));
    }

    private static boolean isAuthenticatedUserAuthorizedToChangeNews(
            Collection<GrantedAuthority> authoritiesOfAuthenticatedUser,
            UUID idOfAuthenticatedUser,
            UUID idOfNewsAuthor) {

        List<String> stringAuthoritiesOfAuthenticatedUser = authoritiesOfAuthenticatedUser.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (stringAuthoritiesOfAuthenticatedUser.contains(RoleConstants.ADMIN)) {
            return true;
        }

        if (!stringAuthoritiesOfAuthenticatedUser.contains(RoleConstants.JOURNALIST)) {
            return false;
        }

        if (idOfAuthenticatedUser == null) {
            return false;
        }

        return idOfAuthenticatedUser.equals(idOfNewsAuthor);
    }
}

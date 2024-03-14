package io.github.sakujj.nms.controller;

import io.github.sakujj.nms.constant.RoleConstants;
import io.github.sakujj.nms.dto.CommentResponse;
import io.github.sakujj.nms.dto.CommentSaveRequest;
import io.github.sakujj.nms.dto.CommentUpdateRequest;
import io.github.sakujj.nms.entity.NewsId;
import io.github.sakujj.nms.service.CommentService;
import io.github.sakujj.nms.service.NewsIdService;
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

import static io.github.sakujj.nms.CommentsServiceApplication.COMMENTS_CONTROLLER_URI;
/**
 * Controller for Comment-microservice.
 * Has dedicated OpenAPI specification at {@link CommentsControllerSpec}
*/
@Validated
@RestController
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
@RequestMapping(value = COMMENTS_CONTROLLER_URI, produces = MediaType.APPLICATION_JSON_VALUE)
public class CommentsController implements CommentsControllerSpec {

    public final static String PAGE_NUMBER_PARAM_NAME = "page";
    public final static String PAGE_NUMBER_DEFAULT_VAL = "0";

    public final static String PAGE_SIZE_PARAM_NAME = "size";
    public final static String PAGE_SIZE_DEFAULT_VAL = "10";

    public final static String CONTAINED_IN_USERNAME_PARAM_NAME = "author-name-having";
    public final static String NEWS_ID_PARAM_NAME = "news-id";


    private final CommentService commentService;

    private final NewsIdService newsIdService;

    @GetMapping(params = {CONTAINED_IN_USERNAME_PARAM_NAME, NEWS_ID_PARAM_NAME})
    public ResponseEntity<Page<CommentResponse>> findAllWithContainedInUsernameAndNewsIdParam(
            @Min(MIN_PAGE_NUMBER)
            @RequestParam(value = PAGE_NUMBER_PARAM_NAME, defaultValue = PAGE_NUMBER_DEFAULT_VAL)
            Integer pageNumber,

            @Min(MIN_PAGE_SIZE)
            @Max(MAX_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAM_NAME, defaultValue = PAGE_SIZE_DEFAULT_VAL)
            Integer pageSize,

            @RequestParam(value = CONTAINED_IN_USERNAME_PARAM_NAME)
            String containedInUsername,

            @RequestParam(value = NEWS_ID_PARAM_NAME)
            UUID newsId) {

        Page<CommentResponse> pageFound = commentService.findByNewsIdAndUsernameContaining(
                newsId,
                containedInUsername,
                pageNumber,
                pageSize);

        return getFoundPageResponseEntity(pageFound);
    }

    @GetMapping(params = {"!" + CONTAINED_IN_USERNAME_PARAM_NAME, NEWS_ID_PARAM_NAME})
    public ResponseEntity<Page<CommentResponse>> findAllWithNewsIdParam(
            @Min(MIN_PAGE_NUMBER)
            @RequestParam(value = PAGE_NUMBER_PARAM_NAME, defaultValue = PAGE_NUMBER_DEFAULT_VAL)
            Integer pageNumber,

            @Min(MIN_PAGE_SIZE)
            @Max(MAX_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAM_NAME, defaultValue = PAGE_SIZE_DEFAULT_VAL)
            Integer pageSize,

            @RequestParam(value = NEWS_ID_PARAM_NAME)
            UUID newsId) {

            Page<CommentResponse> pageFound = commentService.findByNewsId(
                    newsId,
                    pageNumber,
                    pageSize);

            return getFoundPageResponseEntity(pageFound);
    }

    @GetMapping(params = {CONTAINED_IN_USERNAME_PARAM_NAME, "!" + NEWS_ID_PARAM_NAME})
    public ResponseEntity<Page<CommentResponse>> findAllWithContainedInUsername(
            @Min(MIN_PAGE_NUMBER)
            @RequestParam(value = PAGE_NUMBER_PARAM_NAME, defaultValue = PAGE_NUMBER_DEFAULT_VAL)
            Integer pageNumber,

            @Min(MIN_PAGE_SIZE)
            @Max(MAX_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAM_NAME, defaultValue = PAGE_SIZE_DEFAULT_VAL)
            Integer pageSize,

            @RequestParam(value = CONTAINED_IN_USERNAME_PARAM_NAME)
            String containedInUsername) {

        Page<CommentResponse> pageFound = commentService.findByUsernameContaining(
                containedInUsername,
                pageNumber,
                pageSize);

        return getFoundPageResponseEntity(pageFound);
    }

    @GetMapping(params = {"!" + CONTAINED_IN_USERNAME_PARAM_NAME, "!" + NEWS_ID_PARAM_NAME})
    public ResponseEntity<Page<CommentResponse>> findAll(
            @Min(MIN_PAGE_NUMBER)
            @RequestParam(value = PAGE_NUMBER_PARAM_NAME, defaultValue = PAGE_NUMBER_DEFAULT_VAL)
            Integer pageNumber,

            @Min(MIN_PAGE_SIZE)
            @Max(MAX_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAM_NAME, defaultValue = PAGE_SIZE_DEFAULT_VAL)
            Integer pageSize) {

        Page<CommentResponse> pageFound = commentService.findAll(pageNumber, pageSize);

        return getFoundPageResponseEntity(pageFound);
    }

    private static <T> ResponseEntity<Page<T>> getFoundPageResponseEntity(Page<T> pageFound) {
        if (pageFound.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(pageFound);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> findById(@PathVariable("id") UUID id) {

        Optional<CommentResponse> commentResponseOptional = commentService.findById(id);

        return commentResponseOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID commentId, JwtAuthenticationToken idToken) {

        Collection<GrantedAuthority> authoritiesOfAuthenticatedUser = idToken.getAuthorities();

        boolean isAuthenticatedUserAdminOrSubscriber = authoritiesOfAuthenticatedUser.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> switch (authority) {
                    case RoleConstants.ADMIN, RoleConstants.SUBSCRIBER -> true;
                    default -> false;
                });

        if (!isAuthenticatedUserAdminOrSubscriber) {

            return ResponseEntity.noContent().build();
        }

        Optional<CommentResponse> commentResponseOptional = commentService.findById(commentId);
        if (commentResponseOptional.isEmpty()) {

            return ResponseEntity.noContent().build();
        }

        CommentResponse commentResponse = commentResponseOptional.get();
        UUID idOfCommentAuthor = commentResponse.getAuthorId();

        UUID idOfAuthenticatedUser = UUID.fromString(idToken.getName());

        if (!isAuthenticatedUserAuthorizedToChangeComment(authoritiesOfAuthenticatedUser, idOfAuthenticatedUser, idOfCommentAuthor)) {

            return ResponseEntity.noContent().build();
        }

        commentService.deleteById(commentId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @Secured({RoleConstants.ADMIN, RoleConstants.SUBSCRIBER})
    public ResponseEntity<CommentResponse> create(@RequestBody @Valid CommentSaveRequest commentRequest,
                                                  JwtAuthenticationToken idToken) {

        UUID authenticatedUserId = UUID.fromString(idToken.getName());
        String authenticatedUserUsername = KeycloakAuthUtils.getUsernameOfAuthenticatedUserKeycloak(idToken);

        CommentResponse commentResponse = commentService.create(commentRequest, authenticatedUserId, authenticatedUserUsername);

        return ResponseEntity.created(
                        ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(commentResponse.getId())
                                .toUri())
                .body(commentResponse);
    }

    @PutMapping("/{id}")
    @Secured({RoleConstants.ADMIN, RoleConstants.SUBSCRIBER})
    public ResponseEntity<CommentResponse> update(@RequestBody @Valid CommentUpdateRequest commentRequest,
                                                  @PathVariable("id") UUID commentId,
                                                  JwtAuthenticationToken idToken) {

        System.out.println("log");
        Optional<CommentResponse> commentOptional = commentService.findById(commentId);
        if (commentOptional.isEmpty()) {

            if (!idTokenContainsAuthority(idToken, RoleConstants.ADMIN)) {
                throw new AccessDeniedException("You do not have authority to update the specified comment");
            }

            return ResponseEntity.notFound().build();
        }

        CommentResponse commentToUpdate = commentOptional.get();

        UUID idOfAuthor = commentToUpdate.getAuthorId();
        UUID idOfAuthenticatedUser = UUID.fromString(idToken.getName());

        if (!idTokenContainsAuthority(idToken, RoleConstants.ADMIN)
                && idTokenContainsAuthority(idToken, RoleConstants.SUBSCRIBER)) {

            if (!idOfAuthenticatedUser.equals(idOfAuthor)) {
                throw new AccessDeniedException("You do not have authority to update the specified comment");
            }
        }

        return commentService.update(commentId, commentRequest)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/news-ids/{id}")
    public ResponseEntity<Void> deleteNewsId(@PathVariable("id") UUID newsId, JwtAuthenticationToken idToken) {

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

        Optional<NewsId> newsIdOptional = newsIdService.findById(newsId);
        if (newsIdOptional.isEmpty()) {

            return ResponseEntity.noContent().build();
        }

        NewsId newsIdEntity = newsIdOptional.get();
        UUID idOfNewsAuthor = newsIdEntity.getAuthorId();

        UUID idOfAuthenticatedUser = UUID.fromString(idToken.getName());

        if (!isAuthenticatedUserAuthorizedToChangeNews(authoritiesOfAuthenticatedUser, idOfAuthenticatedUser, idOfNewsAuthor)) {

            return ResponseEntity.noContent().build();
        }

        newsIdService.deleteById(newsId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/news-ids")
    @Secured({RoleConstants.ADMIN, RoleConstants.JOURNALIST})
    public ResponseEntity<Void> createNewsId(@RequestBody UUID newsId, JwtAuthenticationToken idToken) {

        UUID authorId = UUID.fromString(idToken.getName());

        newsIdService.create(newsId, authorId);

        return ResponseEntity.noContent().build();
    }


    private static boolean idTokenContainsAuthority(JwtAuthenticationToken idToken, String authority) {

        Collection<GrantedAuthority> authoritiesOfAuthenticatedUser = idToken.getAuthorities();

        return authoritiesOfAuthenticatedUser.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(authority));
    }

    private static boolean isAuthenticatedUserAuthorizedToChangeComment(
            Collection<GrantedAuthority> authoritiesOfAuthenticatedUser,
            UUID idOfAuthenticatedUser,
            UUID idOfCommentAuthor) {

        List<String> stringAuthoritiesOfAuthenticatedUser = authoritiesOfAuthenticatedUser.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (stringAuthoritiesOfAuthenticatedUser.contains(RoleConstants.ADMIN)) {
            return true;
        }

        if (!stringAuthoritiesOfAuthenticatedUser.contains(RoleConstants.SUBSCRIBER)) {
            return false;
        }

        if (idOfAuthenticatedUser == null) {
            return false;
        }

        return idOfAuthenticatedUser.equals(idOfCommentAuthor);
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

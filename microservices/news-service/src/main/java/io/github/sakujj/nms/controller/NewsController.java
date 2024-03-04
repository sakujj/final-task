package io.github.sakujj.nms.controller;

import io.github.sakujj.nms.NewsServiceApplication;
import io.github.sakujj.nms.constant.RoleConstants;
import io.github.sakujj.nms.dto.NewsRequest;
import io.github.sakujj.nms.dto.NewsResponse;
import io.github.sakujj.nms.service.NewsService;
import io.github.sakujj.nms.util.KeycloakAuthUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.server.ResponseStatusException;
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

    @GetMapping
    public ResponseEntity<Page<NewsResponse>> findAll(
            @Min(MIN_PAGE_NUMBER)
            @RequestParam(value = PAGE_NUMBER_PARAM_NAME, defaultValue = PAGE_NUMBER_DEFAULT_VAL)
            Integer pageNumber,

            @Min(MIN_PAGE_SIZE)
            @Max(MAX_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAM_NAME, defaultValue = PAGE_SIZE_DEFAULT_VAL)
            Integer pageSize,

            @RequestParam(required = false, value = CONTAINED_IN_USERNAME_PARAM_NAME)
            String containedInUsername,

            @RequestParam(required = false, value = CONTAINED_IN_TITLE_PARAM_NAME)
            String containedInTitle) {

        if (containedInTitle != null && containedInUsername != null) {
            Page<NewsResponse> pageFound = newsService.findByTitleContainingAndUsernameContaining(
                    containedInTitle,
                    containedInUsername,
                    pageNumber,
                    pageSize);

            return ResponseEntity.ok(pageFound);
        }

        if (containedInUsername != null) {
            Page<NewsResponse> pageFound = newsService.findByUsernameContaining(
                    containedInUsername,
                    pageNumber,
                    pageSize);

            return ResponseEntity.ok(pageFound);
        }

        if (containedInTitle != null) {
            Page<NewsResponse> pageFound = newsService.findByTitleContaining(
                    containedInTitle,
                    pageNumber,
                    pageSize);

            return ResponseEntity.ok(pageFound);
        }

        Page<NewsResponse> pageFound = newsService.findAll(pageNumber, pageSize);

        return ResponseEntity.ok(pageFound);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsResponse> findById(@PathVariable("id") UUID id) {

        NewsResponse found = newsService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(found);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID newsId, JwtAuthenticationToken idToken) {

        Collection<GrantedAuthority> authoritiesOfAuthenticatedUser = idToken.getAuthorities();

        boolean isAuthenticatedUserAdminOrJournalist = authoritiesOfAuthenticatedUser.stream()
                .map(GrantedAuthority::getAuthority)
                .peek(System.out::println)
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

        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @Secured({RoleConstants.ADMIN, RoleConstants.JOURNALIST})
    public ResponseEntity<NewsResponse> create(@RequestBody @Valid NewsRequest newsRequest,
                                               JwtAuthenticationToken idToken) {

        UUID authenticatedUserId = UUID.fromString(idToken.getName());
        String authenticatedUserUsername = KeycloakAuthUtils.getUsernameOfAuthenticatedUserKeycloak(idToken);

        NewsResponse newsResponse = newsService.create(newsRequest, authenticatedUserId, authenticatedUserUsername);

        return ResponseEntity.created(
                        ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(newsResponse.getId())
                                .toUri())
                .body(newsResponse);
    }

    @PutMapping("/{id}")
    @Secured({RoleConstants.ADMIN, RoleConstants.JOURNALIST})
    public ResponseEntity<NewsResponse> replace(@RequestBody @Valid NewsRequest newsRequest,
                                                @PathVariable("id") UUID newsId,
                                                JwtAuthenticationToken idToken) {

        Collection<GrantedAuthority> authoritiesOfAuthenticatedUser = idToken.getAuthorities();
        UUID idOfAuthenticatedUser = UUID.fromString(idToken.getName());
        String usernameOfAuthenticatedUser = KeycloakAuthUtils.getUsernameOfAuthenticatedUserKeycloak(idToken);

        ResponseEntity<NewsResponse> replacedEntity = newsService.replace(
                newsId,
                newsRequest,
                authoritiesOfAuthenticatedUser,
                idOfAuthenticatedUser,
                usernameOfAuthenticatedUser);

        if (HttpStatus.CREATED.isSameCodeAs(replacedEntity.getStatusCode())) {

            return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                            .build()
                            .toUri())
                    .body(replacedEntity.getBody());
        }

        return replacedEntity;
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

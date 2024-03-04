package io.github.sakujj.nms.service;

import io.github.sakujj.nms.dto.NewsRequest;
import io.github.sakujj.nms.dto.NewsResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing {@link io.github.sakujj.nms.entity.News} entity
 */
public interface NewsService {

    /**
     * The method to find news by id
     * @param id
     * @return {@link Optional} with NewsResponse if news are found, or an emtpy {@link Optional}
     * otherwise
     */
    Optional<NewsResponse> findById(UUID id);


    /**
     * The method find the requested page with news
     * @param pageNumber requested page number
     * @param pageSize requested page size
     * @return requested page
     */
    Page<NewsResponse> findAll(int pageNumber, int pageSize);


    /**
     * The method to find the requested page of news, that have @containedInTitle in their title
     * @param containedInTitle string that should be contained in a title
     * @param pageNumber requested page number
     * @param pageSize requested page size
     * @return requested page
     */
    Page<NewsResponse> findByTitleContaining(String containedInTitle, int pageNumber, int pageSize);


    /**
     * The method to find the requested page of news, that have @containedInUsername in their author username
     * @param containedInUsername string that should be contained in an author username
     * @param pageNumber requested page number
     * @param pageSize requested page size
     * @return requested page
     */
    Page<NewsResponse> findByUsernameContaining(String containedInUsername, int pageNumber, int pageSize);


    /**
     *      * The method to find the requested page of news, that have @containedInTitle in their title
     *      AND @containedInUsername in their author username
     * @param containedInTitle string that should be contained in a title
     * @param containedInUsername string that should be contained in an author username
     * @param pageNumber requested page number
     * @param pageSize requested page size
     * @return requested page
     */
    Page<NewsResponse> findByTitleContainingAndUsernameContaining(String containedInTitle,
                                                                  String containedInUsername,
                                                                  int pageNumber,
                                                                  int pageSize);


    /**
     * The method to create a new news instance
     * @param newsRequest the news request
     * @param authorId the id of the author
     * @param username the username of the author
     * @return the representation of the created news instance
     */
    NewsResponse create(NewsRequest newsRequest, UUID authorId, String username);


    /**
     * The method to create a new news instance with an assigned id
     * @param newsRequest the news request
     * @param assignedId the assigned id
     * @param authorId the id of the author
     * @param username the username of the author
     * @return the representation of the created news instance
     */
    NewsResponse createWithAssignedId(NewsRequest newsRequest, UUID assignedId, UUID authorId, String username);


    /**
     * The method used to delete by id
     * @param id
     */
    void deleteById(UUID id);


    /**
     * The method used to replace a news instance.
     * <p>@authoritiesOfAuthenticatedUser, @idOfAuthenticatedUser, @usernameOfAuthenticatedUser should relate
     * to the same user (authenticated user), that makes the request to call this method</p>
     * @param id the id of the news instance to replace
     * @param newsRequest the news request used to replace
     * @param authoritiesOfAuthenticatedUser the authorities of the authenticated user
     * @param idOfAuthenticatedUser the id of the authenticated user
     * @param usernameOfAuthenticatedUser the username of the authenticated user
     * @return response entity with the news response if the replacement happened.
     * The status of the response entity may be OK, CREATED, UNAUTHORIZED, FORBIDDEN
     */
    ResponseEntity<NewsResponse> replace(UUID id,
                                         NewsRequest newsRequest,
                                         Collection<GrantedAuthority> authoritiesOfAuthenticatedUser,
                                         UUID idOfAuthenticatedUser,
                                         String usernameOfAuthenticatedUser);
}

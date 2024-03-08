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
     * Method to find news by id
     * @param id
     * @return {@link Optional} with NewsResponse if news are found, or an emtpy {@link Optional}
     * otherwise
     */
    Optional<NewsResponse> findById(UUID id);


    /**
     * Method to find the requested page with news
     * @param pageNumber requested page number
     * @param pageSize requested page size
     * @return requested page
     */
    Page<NewsResponse> findAll(int pageNumber, int pageSize);


    /**
     * Method to find the requested page of news, that have @containedInTitle in their title
     * @param containedInTitle string that should be contained in a title
     * @param pageNumber requested page number
     * @param pageSize requested page size
     * @return requested page
     */
    Page<NewsResponse> findByTitleContaining(String containedInTitle, int pageNumber, int pageSize);


    /**
     * Method to find the requested page of news, that have @containedInUsername in their author username
     * @param containedInUsername string that should be contained in an author username
     * @param pageNumber requested page number
     * @param pageSize requested page size
     * @return requested page
     */
    Page<NewsResponse> findByUsernameContaining(String containedInUsername, int pageNumber, int pageSize);


    /**
     * Method to find the requested page of news, that have @containedInTitle in their title
     * AND @containedInUsername in their author username
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
     * Method to create a new news instance
     * @param newsRequest the news request
     * @param authorId the id of the author
     * @param username the username of the author
     * @return the representation of the created news instance
     */
    NewsResponse create(NewsRequest newsRequest, UUID authorId, String username);

    /**
     * Method used to delete by id
     * @param id
     */
    void deleteById(UUID id);


    /**
     * Method used to update news.
     * @param id the id of the news instance to update
     * @param newsRequest the news request used to take update information from
     * @return empty {@link Optional} if news instance to update is not present, or {@link Optional} containing
     * news response with updated information.
     */
    Optional<NewsResponse> update(UUID id, NewsRequest newsRequest);

}

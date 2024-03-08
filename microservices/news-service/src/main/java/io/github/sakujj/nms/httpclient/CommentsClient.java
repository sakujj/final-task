package io.github.sakujj.nms.httpclient;

import io.github.sakujj.nms.external.dto.CommentResponse;
import io.github.sakujj.nms.external.dto.CommentSaveRequest;
import io.github.sakujj.nms.external.dto.CommentUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient("comments-service")
public interface CommentsClient {

    String PAGE_NUMBER_PARAM_NAME = "page";
    String PAGE_SIZE_PARAM_NAME = "size";

    String CONTAINED_IN_USERNAME_PARAM_NAME = "author-name-having";
    String NEWS_ID_PARAM_NAME = "news-id";

    @PostMapping("/comments/news-ids")
    ResponseEntity<String> createNewsId(@RequestBody UUID id,
                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeaderValue);

    @DeleteMapping("/comments/news-ids/{id}")
    ResponseEntity<String> deleteNewsId(@PathVariable("id") UUID id,
                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeaderValue);

    @GetMapping("/comments")
    ResponseEntity<Page<CommentResponse>> findAll(
            @RequestParam(value = PAGE_NUMBER_PARAM_NAME)
            Integer pageNumber,

            @RequestParam(value = PAGE_SIZE_PARAM_NAME)
            Integer pageSize,

            @RequestParam(required = false, value = CONTAINED_IN_USERNAME_PARAM_NAME)
            String containedInUsername,

            @RequestParam(required = false, value = NEWS_ID_PARAM_NAME)
            UUID newsId);


    @GetMapping("/comments/{id}")
    ResponseEntity<CommentResponse> findById(@PathVariable("id") UUID id);


    @DeleteMapping("/comments/{id}")
    ResponseEntity<?> delete(@PathVariable("id") UUID commentId,
                             @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeaderValue);


    @PostMapping("/comments")
    ResponseEntity<CommentResponse> create(@RequestBody CommentSaveRequest commentRequest,
                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeaderValue);


    @PutMapping("/comments/{id}")
    ResponseEntity<CommentResponse> update(@RequestBody CommentUpdateRequest commentRequest,
                                           @PathVariable("id") UUID commentId,
                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeaderValue);
}

package io.github.sakujj.nms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

public interface CommentsControllerSpec {

    int MIN_PAGE_NUMBER = 0;

    int MIN_PAGE_SIZE = 1;
    int MAX_PAGE_SIZE = 250;


    ResponseEntity<Void> deleteNewsId(@PathVariable("id") UUID commentId, JwtAuthenticationToken idToken);

    ResponseEntity<Void> createNewsId(@RequestBody UUID newsId, JwtAuthenticationToken idToken);
}

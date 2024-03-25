package io.github.sakujj.nms.controller;

import io.github.sakujj.nms.dto.NewsRequest;
import io.github.sakujj.nms.dto.NewsResponse;
import io.github.sakujj.nms.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Tag(name = "News")
public interface NewsControllerSpec {

    int MIN_PAGE_NUMBER = 0;

    int MIN_PAGE_SIZE = 1;
    int MAX_PAGE_SIZE = 250;

    @Operation(summary = "find a page with news sorted DESC by creation time", responses = {
            @ApiResponse(
                    responseCode = HttpStatus.SC_OK + "",
                    description = "The requested page is found.",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = HttpStatus.SC_BAD_REQUEST + "",
                    description = "An invalid page number OR an invalid page size.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<Page<NewsResponse>> findAll(
            @Min(MIN_PAGE_NUMBER)
            Integer pageNumber,

            @Min(MIN_PAGE_SIZE)
            @Max(MAX_PAGE_SIZE)
            Integer pageSize);


    @Operation(responses = {
            @ApiResponse(
                    responseCode = HttpStatus.SC_OK + "",
                    description = "The news are found.",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = HttpStatus.SC_NOT_FOUND + "",
                    description = "The news are not found.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = HttpStatus.SC_BAD_REQUEST + "",
                    description = "A malformed uuid is passed.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<NewsResponse> findById(@PathVariable("id") UUID id);


    @Operation(responses = {
            @ApiResponse(
                    responseCode = HttpStatus.SC_NO_CONTENT + "",
                    description = "The news was successfully deleted or did not exist or you are not authorized.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = HttpStatus.SC_BAD_REQUEST + "",
                    description = "The news can not be deleted due to certain constraints.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    ResponseEntity<Void> delete(@PathVariable("id") UUID newsId,

                             @Parameter(hidden = true)
                             JwtAuthenticationToken idToken);


    @Operation(responses = {
            @ApiResponse(
                    responseCode = HttpStatus.SC_CREATED + "",
                    description = "The new news were successfully created.",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = HttpStatus.SC_BAD_REQUEST + "",
                    description = "An invalid request body.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = HttpStatus.SC_UNAUTHORIZED + "",
                    description = "You are not authenticated.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = HttpStatus.SC_FORBIDDEN + "",
                    description = "You do not have needed authorities.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    ResponseEntity<NewsResponse> create(@io.swagger.v3.oas.annotations.parameters.RequestBody
                                        @Valid
                                        NewsRequest newsRequest,

                                        @Parameter(hidden = true)
                                        JwtAuthenticationToken idToken);


    @Operation(responses = {
            @ApiResponse(
                    responseCode = HttpStatus.SC_CREATED + "",
                    description = "The new news were successfully created.",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = HttpStatus.SC_OK + "",
                    description = "The new news were successfully replaced.",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = HttpStatus.SC_BAD_REQUEST + "",
                    description = "An invalid request body.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = HttpStatus.SC_UNAUTHORIZED + "",
                    description = "You are not authenticated.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = HttpStatus.SC_FORBIDDEN + "",
                    description = "You do not have needed authorities.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    ResponseEntity<NewsResponse> update(@io.swagger.v3.oas.annotations.parameters.RequestBody
                                         @Valid
                                         NewsRequest newsRequest,

                                        @PathVariable("id") UUID newsId,

                                        @Parameter(hidden = true)
                                         JwtAuthenticationToken idToken);

}

package io.github.sakujj.nms.integration.httpclient;

import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.sakujj.nms.httpclient.CommentsClient;
import io.github.sakujj.nms.integration.testcontainer.CommonPostgresContainerInitializer;
import io.github.sakujj.nms.util.AuthUtils;
import io.github.sakujj.nms.util.NewsTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CommentsClientTests extends CommonPostgresContainerInitializer {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig())
            .build();

    @Autowired
    private CommentsClient commentsClient;

    @DynamicPropertySource
    static void configureFeignClient(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.openfeign.client.config.comments-service.url", () -> {
            return wireMockExtension.getRuntimeInfo().getHttpBaseUrl();
        });
    }

    @Test
    void shouldGetCorrectResponseToCreateRequest() {

        // given
        HttpStatus expectedStatus = HttpStatus.OK;
        Body expectedBody = new Body("expected 33 body");

        UUID newsId = NewsTestBuilder.anArticle().getId();
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        wireMockExtension.stubFor(
                post(urlPathMatching("/comments/news-ids"))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + token))
                        .withRequestBody(containing(newsId.toString()))
                        .willReturn(aResponse()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                                .withResponseBody(expectedBody)
                                .withStatus(expectedStatus.value()))
        );

        // when
        ResponseEntity<String> response = commentsClient.createNewsId(newsId, AuthUtils.getBearerAuthHeaderValue(token));

        // then
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isEqualTo(expectedBody.asString());
    }

    @Test
    void shouldGetCorrectResponseToDeleteRequest() {

        // given
        HttpStatus expectedStatus = HttpStatus.OK;
        Body expectedBody = new Body("expected 334 body");

        UUID newsId = NewsTestBuilder.anArticle().getId();

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        wireMockExtension.stubFor(
                delete(urlPathMatching("/comments/news-ids/" + newsId))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + token))
                        .willReturn(aResponse()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                                .withBody(expectedBody.asString())
                                .withStatus(expectedStatus.value()))
        );

        // when
        ResponseEntity<String> response = commentsClient.deleteNewsId(newsId, AuthUtils.getBearerAuthHeaderValue(token));

        // then
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isEqualTo(expectedBody.asString());
    }
}

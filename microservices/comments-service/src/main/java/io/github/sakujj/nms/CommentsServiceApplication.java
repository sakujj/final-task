package io.github.sakujj.nms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class CommentsServiceApplication {

    public static final String COMMENTS_CONTROLLER_URI = "/comments";

    public static void main(String[] args) {

        SpringApplication.run(CommentsServiceApplication.class, args);
    }
}

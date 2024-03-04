package io.github.sakujj.nms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
public class NewsServiceApplication {

    public static final String NEWS_CONTROLLER_URI = "/news";

    public static void main(String[] args) {

        SpringApplication.run(NewsServiceApplication.class, args);
    }
}

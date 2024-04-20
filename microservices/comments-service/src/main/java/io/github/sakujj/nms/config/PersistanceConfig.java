package io.github.sakujj.nms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(basePackages = "io.github.sakujj.nms.repository")
public class PersistanceConfig {
}

package io.github.sakujj.nms.integration.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@TestConfiguration
@ComponentScan(basePackages = "io.github.sakujj.nms.controller")
public class MockMvcConfig {
}

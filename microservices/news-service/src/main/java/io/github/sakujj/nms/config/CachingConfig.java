package io.github.sakujj.nms.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sakujj.nms.dto.NewsResponse;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.Map;

@EnableCaching
@Configuration(proxyBeanMethods = false)
public class CachingConfig {

    public static final String NEWS_CACHE_NAME = "news-cache";

    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {

        Jackson2JsonRedisSerializer<NewsResponse> serializer = new Jackson2JsonRedisSerializer<>(objectMapper,
                NewsResponse.class);

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(RedisCacheWriter.lockingRedisCacheWriter(connectionFactory))
                .withInitialCacheConfigurations(Map.of(NEWS_CACHE_NAME, cacheConfig))
                .enableStatistics()
                .build();
    }
}


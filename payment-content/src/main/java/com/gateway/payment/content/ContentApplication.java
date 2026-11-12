package com.gateway.payment.content;

import com.gateway.payment.content.config.ContentCacheProperties;
import com.gateway.payment.content.config.ContentMediaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {
        // Redis activé explicitement via ContentRedisConfig si content.cache.redis-enabled=true
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class
})
@EnableConfigurationProperties({ContentMediaProperties.class, ContentCacheProperties.class})
public class ContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }
}

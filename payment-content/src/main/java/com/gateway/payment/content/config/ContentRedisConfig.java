package com.gateway.payment.content.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@ConditionalOnProperty(name = "content.cache.redis-enabled", havingValue = "true")
@EnableConfigurationProperties(RedisProperties.class)
public class ContentRedisConfig {

    @Bean
    LettuceConnectionFactory contentRedisConnectionFactory(RedisProperties properties) {
        RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
        conf.setHostName(properties.getHost());
        conf.setPort(properties.getPort());
        if (properties.getPassword() != null && !properties.getPassword().isBlank()) {
            conf.setPassword(properties.getPassword());
        }
        conf.setDatabase(properties.getDatabase());
        return new LettuceConnectionFactory(conf);
    }

    @Bean
    StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory contentRedisConnectionFactory) {
        return new StringRedisTemplate(contentRedisConnectionFactory);
    }
}

package com.gateway.payment.common.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * Redis optionnel pour hot path (activé via {@code gateway.performance.redis-enabled=true}).
 */
@Configuration
@ConditionalOnProperty(name = "gateway.performance.redis-enabled", havingValue = "true")
public class GatewayRedisConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayRedisConfig.class);

    @Bean
    LettuceConnectionFactory gatewayRedisConnectionFactory(RedisProperties props) {
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
        standalone.setHostName(props.getHost() != null ? props.getHost() : "localhost");
        standalone.setPort(props.getPort() > 0 ? props.getPort() : 6379);
        if (props.getPassword() != null) {
            standalone.setPassword(props.getPassword());
        }
        if (props.getDatabase() > 0) {
            standalone.setDatabase(props.getDatabase());
        }

        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofMillis(500))
                .build();
        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                .autoReconnect(true)
                .build();
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofMillis(800))
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone, clientConfig);
        factory.afterPropertiesSet();
        log.info("Redis performance enabled (locks/claims/idempotence) host={}:{}", standalone.getHostName(), standalone.getPort());
        return factory;
    }

    @Bean
    StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory gatewayRedisConnectionFactory) {
        return new StringRedisTemplate(gatewayRedisConnectionFactory);
    }
}

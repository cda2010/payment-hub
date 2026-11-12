package com.gateway.payment.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(GatewayPerformanceProperties.class)
public class HttpClientConfig {

    /**
     * Client HTTP partagé vers les connecteurs FSE (keep-alive / pool JDK).
     */
    @Bean
    @Primary
    RestClient gatewayRestClient(GatewayPerformanceProperties performance) {
        GatewayPerformanceProperties.Http props = performance.getHttp();
        Executor executor = createHttpExecutor(performance);
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.getConnectTimeoutMs()))
                .version(HttpClient.Version.HTTP_1_1)
                .executor(executor)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(props.getReadTimeoutMs()));
        return RestClient.builder().requestFactory(factory).build();
    }

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Tomcat on virtual threads (Java 21+) — plus de threads OS bloqués sur I/O.
     */
    @Bean
    TomcatProtocolHandlerCustomizer<?> virtualThreadTomcatCustomizer(GatewayPerformanceProperties performance) {
        return protocolHandler -> {
            if (performance.isVirtualThreads() && supportsVirtualThreads()) {
                protocolHandler.setExecutor(newVirtualThreadExecutor());
            }
        };
    }

    static ExecutorService createHttpExecutor(GatewayPerformanceProperties performance) {
        if (performance.isVirtualThreads() && supportsVirtualThreads()) {
            return newVirtualThreadExecutor();
        }
        return Executors.newFixedThreadPool(Math.max(16, performance.getHttp().getExecutorThreads()));
    }

    static boolean supportsVirtualThreads() {
        try {
            Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
            return Runtime.version().feature() >= 21;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    static ExecutorService newVirtualThreadExecutor() {
        try {
            Method m = Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
            return (ExecutorService) m.invoke(null);
        } catch (ReflectiveOperationException e) {
            return Executors.newCachedThreadPool();
        }
    }
}

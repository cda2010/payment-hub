package io.github.jhipster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "jhipster")
public class JHipsterProperties {

    private final Async async = new Async();
    private final Http http = new Http();
    private final Cache cache = new Cache();
    private final Mail mail = new Mail();
    private final Metrics metrics = new Metrics();
    private final Logging logging = new Logging();
    private final CorsConfiguration cors = defaultCors();

    public Async getAsync() {
        return async;
    }

    public Http getHttp() {
        return http;
    }

    public Cache getCache() {
        return cache;
    }

    public Mail getMail() {
        return mail;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public Logging getLogging() {
        return logging;
    }

    public CorsConfiguration getCors() {
        return cors;
    }

    private static CorsConfiguration defaultCors() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:4213",
                "http://127.0.0.1:4213",
                "http://localhost:4210",
                "http://127.0.0.1:4210",
                "http://localhost:9000",
                "http://127.0.0.1:9000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        return config;
    }

    public static class Async {
        private int corePoolSize = 2;
        private int maxPoolSize = 50;
        private int queueCapacity = 10000;

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }
    }

    public static class Http {
        public enum Version {
            V_1_1, V_2_0
        }

        private Version version = Version.V_1_1;

        public Version getVersion() {
            return version;
        }

        public void setVersion(Version version) {
            this.version = version;
        }
    }

    public static class Cache {
        private final Ehcache ehcache = new Ehcache();

        public Ehcache getEhcache() {
            return ehcache;
        }

        public static class Ehcache {
            private long maxEntries = 1000;
            private int timeToLiveSeconds = 3600;

            public long getMaxEntries() {
                return maxEntries;
            }

            public void setMaxEntries(long maxEntries) {
                this.maxEntries = maxEntries;
            }

            public int getTimeToLiveSeconds() {
                return timeToLiveSeconds;
            }

            public void setTimeToLiveSeconds(int timeToLiveSeconds) {
                this.timeToLiveSeconds = timeToLiveSeconds;
            }
        }
    }

    public static class Mail {
        private String from = "pti@localhost";
        private String baseUrl = "http://localhost:9002";

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Metrics {
        private final Jmx jmx = new Jmx();
        private final Logs logs = new Logs();

        public Jmx getJmx() {
            return jmx;
        }

        public Logs getLogs() {
            return logs;
        }

        public static class Jmx {
            private boolean enabled;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }

        public static class Logs {
            private boolean enabled;
            private long reportFrequency = 60;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public long getReportFrequency() {
                return reportFrequency;
            }

            public void setReportFrequency(long reportFrequency) {
                this.reportFrequency = reportFrequency;
            }
        }
    }

    public static class Logging {
        private final Logstash logstash = new Logstash();

        public Logstash getLogstash() {
            return logstash;
        }

        public static class Logstash {
            private boolean enabled;
            private String host = "localhost";
            private int port = 5000;
            private int queueSize = 512;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public int getQueueSize() {
                return queueSize;
            }

            public void setQueueSize(int queueSize) {
                this.queueSize = queueSize;
            }
        }
    }

    @SuppressWarnings("unused")
    public void setCorsAllowedOrigins(List<String> origins) {
        if (origins != null && !origins.isEmpty()) {
            cors.setAllowedOrigins(new ArrayList<>(origins));
        }
    }
}

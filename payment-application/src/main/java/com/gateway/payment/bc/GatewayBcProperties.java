package com.gateway.payment.bc;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * URLs des BC peers (pricing, rules, commission, network, alias, documents, identity).
 */
@ConfigurationProperties(prefix = "gateway.bc")
public record GatewayBcProperties(
        Pricing pricing,
        Rules rules,
        Commission commission,
        Network network,
        Alias alias,
        Documents documents,
        Identity identity,
        boolean pricingEnabled,
        boolean rulesEnabled,
        boolean commissionEnabled,
        boolean networkEnabled,
        boolean aliasEnabled,
        boolean documentsEnabled,
        boolean identityEnabled
) {
    public GatewayBcProperties {
        if (pricing == null) {
            pricing = new Pricing("http://localhost:8095", "PT5S");
        }
        if (rules == null) {
            rules = new Rules("http://localhost:8099", "PT5S");
        }
        if (commission == null) {
            commission = new Commission("http://localhost:8100", "PT5S");
        }
        if (network == null) {
            network = new Network("http://localhost:8096", "PT5S");
        }
        if (alias == null) {
            alias = new Alias("http://localhost:8097", "PT5S");
        }
        if (documents == null) {
            documents = new Documents("http://localhost:8101", "PT15S");
        }
        if (identity == null) {
            identity = new Identity("http://localhost:8094", "PT5S");
        }
    }

    public record Pricing(String url, String timeout) {
        public Pricing {
            if (url == null || url.isBlank()) {
                url = "http://localhost:8095";
            }
            if (timeout == null || timeout.isBlank()) {
                timeout = "PT5S";
            }
        }
    }

    public record Rules(String url, String timeout) {
        public Rules {
            if (url == null || url.isBlank()) {
                url = "http://localhost:8099";
            }
            if (timeout == null || timeout.isBlank()) {
                timeout = "PT5S";
            }
        }
    }

    public record Commission(String url, String timeout) {
        public Commission {
            if (url == null || url.isBlank()) {
                url = "http://localhost:8100";
            }
            if (timeout == null || timeout.isBlank()) {
                timeout = "PT5S";
            }
        }
    }

    public record Network(String url, String timeout) {
        public Network {
            if (url == null || url.isBlank()) {
                url = "http://localhost:8096";
            }
            if (timeout == null || timeout.isBlank()) {
                timeout = "PT5S";
            }
        }
    }

    public record Alias(String url, String timeout) {
        public Alias {
            if (url == null || url.isBlank()) {
                url = "http://localhost:8097";
            }
            if (timeout == null || timeout.isBlank()) {
                timeout = "PT5S";
            }
        }
    }

    public record Documents(String url, String timeout) {
        public Documents {
            if (url == null || url.isBlank()) {
                url = "http://localhost:8101";
            }
            if (timeout == null || timeout.isBlank()) {
                timeout = "PT15S";
            }
        }
    }

    public record Identity(String url, String timeout) {
        public Identity {
            if (url == null || url.isBlank()) {
                url = "http://localhost:8094";
            }
            if (timeout == null || timeout.isBlank()) {
                timeout = "PT5S";
            }
        }
    }
}

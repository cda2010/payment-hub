package com.gateway.payment.datasources;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Active {@link RemoteDataSourceProperties} et expose le SPI catalogue
 * ({@link DataSourceResolveService}, {@code spi.*}) sous {@code com.gateway.payment.datasources}.
 * Les beans sont scannés via {@code com.gateway.payment} (PaymentGatewayApplication)
 * ou ce package.
 */
@AutoConfiguration
@EnableConfigurationProperties(RemoteDataSourceProperties.class)
public class RemoteDataSourceAutoConfiguration {
}

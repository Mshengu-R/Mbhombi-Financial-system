package com.banking.APIgateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver keyResolver() {
        return exchange -> exchange.getPrincipal()
                .cast(Authentication.class)
                .map(Authentication::getName)
                .switchIfEmpty(Mono.fromSupplier(() -> remoteAddress(exchange)));
    }

    // This method retrieves the remote address of the client making the request. If the remote address is not available, it returns "unknown-client".
    private String remoteAddress(ServerWebExchange exchange) {
        if (exchange.getRequest().getRemoteAddress() == null
                || exchange.getRequest().getRemoteAddress().getAddress() == null) {
            return "unknown-client";
        }
        return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    }
}
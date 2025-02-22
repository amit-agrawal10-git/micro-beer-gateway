package com.github.amag.microbeergateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile({"local-discovery", "digitalocean"})
@Configuration
public class LoadBalancedRouteConfig {

    @Bean
    public RouteLocator loadBalancedRoutes(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/api/v1/beer*","/api/v1/beer/*", "/api/v1/beerUpc/*")
                    .uri("lb://beer-service")
                    .id("beer-service"))
                .route(r -> r.path("/api/v1/customers/**")
                        .uri("lb://beer-order-service")
                        .id("order-service"))
                .route(r -> r.path("/api/v1/beer/*/inventory")
                        .filters(f -> f.circuitBreaker( c ->
                                c.setName("inventoryCB")
                                .setFallbackUri("forward:/inventory-failover")
                                .setRouteId("inventory-fallback")))
                        .uri("lb://beer-inventory-service")
                        .id("inventory-service"))
                .route(r -> r.path("/inventory-failover/**")
                        .uri("lb://inventory-failover")
                        .id("inventory-failover"))
                .build();
    }
}

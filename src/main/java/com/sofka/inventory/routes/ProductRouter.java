package com.sofka.inventory.routes;

import com.sofka.inventory.handlers.ProductHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@AllArgsConstructor
public class ProductRouter {
    private ProductHandler handler;

    @Bean
    public RouterFunction<ServerResponse> productRoutes() {
        return RouterFunctions.route()
            .POST("/products", handler::create)
            .build();
    }
}

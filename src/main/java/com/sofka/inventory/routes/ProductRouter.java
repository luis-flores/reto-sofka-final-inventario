package com.sofka.inventory.routes;

import com.sofka.inventory.handlers.InventoryHandler;
import com.sofka.inventory.handlers.ProductHandler;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
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
    @RouterOperations({
        @RouterOperation(path = "/products", beanClass = ProductHandler.class, beanMethod = "create"),
        @RouterOperation(path = "/products/{page}", beanClass = ProductHandler.class, beanMethod = "list")
    })
    public RouterFunction<ServerResponse> productRoutes() {
        return RouterFunctions.route()
            .POST("/products", handler::create)
            .GET("/products/{page}", handler::list)
            .build();
    }
}

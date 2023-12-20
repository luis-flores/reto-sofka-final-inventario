package com.sofka.inventory.routes;

import com.sofka.inventory.handlers.InventoryHandler;
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
public class InventoryRouter {
    private InventoryHandler handler;

    @Bean
    @RouterOperations({
        @RouterOperation(path = "/product/inventory/add", beanClass = InventoryHandler.class, beanMethod = "addInventoryPerProduct"),
        @RouterOperation(path = "/product/inventory/add_many", beanClass = InventoryHandler.class, beanMethod = "addInventoryToManyProducts")
    })
    public RouterFunction<ServerResponse> inventoryRoutes() {
        return RouterFunctions.route()
            .POST("/product/inventory/add", handler::addInventoryPerProduct)
            .POST("/product/inventory/add_many", handler::addInventoryToManyProducts)
            .build();
    }
}

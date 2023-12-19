package com.sofka.inventory.routes;

import com.sofka.inventory.handlers.InventoryHandler;
import lombok.AllArgsConstructor;
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
    public RouterFunction<ServerResponse> inventoryRoutes() {
        return RouterFunctions.route()
            .POST("/product/inventory/add", handler::addInventoryPerProduct)
            .build();
    }
}

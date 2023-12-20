package com.sofka.inventory.routes;

import com.sofka.inventory.handlers.InventoryHandler;
import com.sofka.inventory.handlers.SaleHandler;
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
public class SaleRouter {
    private SaleHandler handler;

    @Bean
    @RouterOperations({
        @RouterOperation(path = "/sale/retail", beanClass = SaleHandler.class, beanMethod = "retailSale"),
        @RouterOperation(path = "/sale/wholesale", beanClass = SaleHandler.class, beanMethod = "wholesale")
    })
    public RouterFunction<ServerResponse> saleRoutes() {
        return RouterFunctions.route()
            .POST("/sale/retail", handler::retailSale)
            .POST("/sale/wholesale", handler::wholesale)
            .build();
    }
}

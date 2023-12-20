package com.sofka.inventory.routes;

import com.sofka.inventory.handlers.SaleHandler;
import lombok.AllArgsConstructor;
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
    public RouterFunction<ServerResponse> saleRoutes() {
        return RouterFunctions.route()
            .POST("/sale/retail", handler::retailSale)
            .POST("/sale/wholesale", handler::wholesale)
            .build();
    }
}

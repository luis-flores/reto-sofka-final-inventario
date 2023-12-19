package com.sofka.inventory.handlers;

import com.sofka.inventory.models.dto.InventoryDTO;
import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.useCases.InventoryAddPerProductUseCase;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class InventoryHandler {
    InventoryAddPerProductUseCase inventoryAddPerProductUseCase;

    public Mono<ServerResponse> addInventoryPerProduct(ServerRequest request) {
        return request.bodyToMono(InventoryDTO.class)
            .flatMap(inventoryDTO -> ServerResponse.ok()
                .body(inventoryAddPerProductUseCase.apply(inventoryDTO), ProductDTO.class)
            );
    }
}

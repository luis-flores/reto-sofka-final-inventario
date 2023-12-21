package com.sofka.inventory.handlers;

import com.sofka.inventory.models.dto.InventoryDTO;
import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.models.exceptions.ApplicationException;
import com.sofka.inventory.useCases.InventoryAddPerProductUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@AllArgsConstructor
public class InventoryHandler {
    InventoryAddPerProductUseCase inventoryAddPerProductUseCase;

    public Mono<ServerResponse> addInventoryPerProduct(ServerRequest request) {
        return request.bodyToMono(InventoryDTO.class)
            .flatMap(inventoryDTO -> {
                Mono<ProductDTO> data = inventoryAddPerProductUseCase.apply(inventoryDTO)
                    .onErrorMap(ApplicationException.class, ex -> new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage()));

                return ServerResponse.ok()
                    .body(data, ProductDTO.class);
            });
    }

    public Mono<ServerResponse> addInventoryToManyProducts(ServerRequest request) {
        Mono<List<ProductDTO>> data = request.bodyToFlux(InventoryDTO.class)
            .flatMap(inventory -> inventoryAddPerProductUseCase.apply(inventory))
            .onErrorMap(ApplicationException.class, ex -> new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage()))
            .collectList();

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(data, ProductDTO.class);
    }
}

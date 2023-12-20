package com.sofka.inventory.handlers;

import com.sofka.inventory.models.dto.InventoryDTO;
import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.useCases.InventoryAddPerProductUseCase;
import com.sofka.inventory.useCases.SaleAddRetailUseCase;
import com.sofka.inventory.useCases.SaleAddWholesaleUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class SaleHandler {
    SaleAddRetailUseCase saleAddRetailUseCase;
    SaleAddWholesaleUseCase saleAddWholesaleUseCase;

    public Mono<ServerResponse> retailSale(ServerRequest request) {
        Flux<InventoryDTO> data = request.bodyToFlux(InventoryDTO.class);

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(saleAddRetailUseCase.apply(data), ProductDTO.class);
    }

    public Mono<ServerResponse> wholesale(ServerRequest request) {
        Flux<InventoryDTO> data = request.bodyToFlux(InventoryDTO.class);

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(saleAddWholesaleUseCase.apply(data), ProductDTO.class);
    }
}

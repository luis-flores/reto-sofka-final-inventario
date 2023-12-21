package com.sofka.inventory.handlers;

import com.sofka.inventory.models.dto.InventoryDTO;
import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.models.exceptions.ApplicationException;
import com.sofka.inventory.useCases.SaleAddRetailUseCase;
import com.sofka.inventory.useCases.SaleAddWholesaleUseCase;
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
public class SaleHandler {
    SaleAddRetailUseCase saleAddRetailUseCase;
    SaleAddWholesaleUseCase saleAddWholesaleUseCase;

    public Mono<ServerResponse> retailSale(ServerRequest request) {
        Flux<InventoryDTO> formData = request.bodyToFlux(InventoryDTO.class);
        Mono<List<ProductDTO>> data = saleAddRetailUseCase.apply(formData)
            .onErrorMap(ApplicationException.class, ex -> new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage()))
            .collectList();

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(data, ProductDTO.class);
    }

    public Mono<ServerResponse> wholesale(ServerRequest request) {
        Flux<InventoryDTO> formData = request.bodyToFlux(InventoryDTO.class);
        Mono<List<ProductDTO>> data = saleAddWholesaleUseCase.apply(formData)
            .onErrorMap(ApplicationException.class, ex -> new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage()))
            .collectList();

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(data, ProductDTO.class);
    }
}

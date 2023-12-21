package com.sofka.inventory.handlers;

import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.models.exceptions.ApplicationException;
import com.sofka.inventory.useCases.ProductCreateUseCase;
import com.sofka.inventory.useCases.ProductListUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ProductHandler {
    private ProductCreateUseCase productCreateUseCase;
    private ProductListUseCase productListUseCase;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(ProductDTO.class)
            .flatMap(product -> {
                Mono<ProductDTO> data = productCreateUseCase.apply(product)
                    .onErrorMap(ApplicationException.class, ex -> new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage()));

                return ServerResponse.ok()
                    .body(data, ProductDTO.class);
            });
    }

    public Mono<ServerResponse> list(ServerRequest request) {
        int page = Integer.parseInt(request.pathVariable("page"));

        return ServerResponse.ok()
            .contentType(MediaType.TEXT_EVENT_STREAM)
            .body(productListUseCase.apply(page), ProductDTO.class);
    }
}

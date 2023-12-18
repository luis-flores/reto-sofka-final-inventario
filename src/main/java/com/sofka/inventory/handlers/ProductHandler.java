package com.sofka.inventory.handlers;

import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.useCases.ProductCreateUseCase;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ProductHandler {
    private ProductCreateUseCase productCreateUseCase;
    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(ProductDTO.class)
            .flatMap(product -> ServerResponse.ok()
                .body(productCreateUseCase.apply(product), ProductDTO.class)
            );
    }
}

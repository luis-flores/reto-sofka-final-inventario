package com.sofka.inventory.routes;

import com.sofka.inventory.handlers.ProductHandler;
import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.useCases.ProductCreateUseCase;
import com.sofka.inventory.useCases.ProductListUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductRouterTests {
    private WebTestClient webTestClient;
    @Mock
    private ProductCreateUseCase productCreateUseCase;
    @Mock
    private ProductListUseCase productListUseCase;
    private ProductHandler productHandler;
    private ProductRouter productRouter;

    @BeforeEach
    public void setup() {
        productHandler = new ProductHandler(productCreateUseCase, productListUseCase);
        productRouter = new ProductRouter(productHandler);
        webTestClient = WebTestClient.bindToRouterFunction(productRouter.productRoutes())
            .build();
    }

    @Test
    void ProductCreateRouteTest() {
        ProductDTO productDTO = new ProductDTO();
        when(productCreateUseCase.apply(productDTO)).thenReturn(Mono.just(productDTO));

        webTestClient.post()
            .uri("/products")
            .bodyValue(productDTO)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ProductDTO.class)
            .isEqualTo(productDTO);
    }

    @Test
    void ProductListRouteTest() {
        ProductDTO productDTO = new ProductDTO();
        when(productListUseCase.apply(1)).thenReturn(Flux.just(productDTO));

        webTestClient.get()
            .uri("/products/1")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(ProductDTO.class)
            .isEqualTo(List.of(productDTO));
    }
}

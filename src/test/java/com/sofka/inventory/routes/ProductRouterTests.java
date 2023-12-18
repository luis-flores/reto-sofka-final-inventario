package com.sofka.inventory.routes;

import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.useCases.ProductCreateUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductRouterTests {
    private WebTestClient webTestClient;
    @Mock
    private ProductCreateUseCase productCreateUseCase;
    private ProductHandler productHandler;
    private ProductRouter productRouter;

    @BeforeEach
    public void setup() {
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
}

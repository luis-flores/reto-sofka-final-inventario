package com.sofka.inventory.routes;

import com.sofka.inventory.handlers.InventoryHandler;
import com.sofka.inventory.models.dto.InventoryDTO;
import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.useCases.InventoryAddPerProductUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventoryRouterTests {
    private WebTestClient webTestClient;
    @Mock
    private InventoryAddPerProductUseCase inventoryAddPerProductUseCase;
    private InventoryHandler inventoryHandler;
    private InventoryRouter inventoryRouter;

    @BeforeEach
    public void setup() {
        inventoryHandler = new InventoryHandler(inventoryAddPerProductUseCase);
        inventoryRouter = new InventoryRouter(inventoryHandler);
        webTestClient = WebTestClient.bindToRouterFunction(inventoryRouter.inventoryRoutes())
            .build();
    }

    @Test
    void InventoryAddPerProductRouteTest() {
        InventoryDTO inventoryDTO = new InventoryDTO();
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId("<product-id>");
        productDTO.setQuantity(10);
        productDTO.setEnabled(true);
        inventoryDTO.setProduct(productDTO);
        inventoryDTO.setQuantity(5);
        when(inventoryAddPerProductUseCase.apply(inventoryDTO)).thenReturn(Mono.just(productDTO));

        webTestClient.post()
            .uri("/product/inventory/add")
            .bodyValue(inventoryDTO)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ProductDTO.class)
            .isEqualTo(productDTO);
    }
}

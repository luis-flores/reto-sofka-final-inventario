package com.sofka.inventory.routes;

import com.sofka.inventory.handlers.InventoryHandler;
import com.sofka.inventory.models.dto.InventoryDTO;
import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.useCases.InventoryAddPerProductUseCase;
import com.sofka.inventory.useCases.SaleAddRetailUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SaleRouterTests {
    private WebTestClient webTestClient;
    @Mock
    private SaleAddRetailUseCase saleAddRetailUseCase;
    private SaleHandler saleHandler;
    private SaleRouter saleRouter;

    @BeforeEach
    public void setup() {
        saleHandler = new InventoryHandler(saleAddRetailUseCase);
        saleRouter = new InventoryRouter(saleHandler);
        webTestClient = WebTestClient.bindToRouterFunction(saleRouter.saleRoutes())
            .build();
    }

    @Test
    void SaleAddRetailRouteTest() {
        InventoryDTO inventoryDTO = new InventoryDTO();
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId("<product-id>");
        productDTO.setQuantity(10);
        productDTO.setEnabled(true);
        inventoryDTO.setProduct(productDTO);
        inventoryDTO.setQuantity(5);
        when(saleAddRetailUseCase.apply(Flux.just(inventoryDTO))).thenReturn(Flux.just(productDTO));

        webTestClient.post()
            .uri("/product/inventory/add")
            .bodyValue(inventoryDTO)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ProductDTO.class)
            .isEqualTo(productDTO);
    }
}

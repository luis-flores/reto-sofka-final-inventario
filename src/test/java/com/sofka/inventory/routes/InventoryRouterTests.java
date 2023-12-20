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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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

    @Test
    void InventoryAddToManyProductsRouteTest() {
        ProductDTO productDTO1 = new ProductDTO();
        productDTO1.setId("<product-id>");
        productDTO1.setQuantity(10);
        productDTO1.setEnabled(true);

        InventoryDTO inventoryDTO1 = new InventoryDTO();
        inventoryDTO1.setProduct(productDTO1);
        inventoryDTO1.setQuantity(5);

        ProductDTO productDTO2 = new ProductDTO();
        productDTO2.setId("<product-id>");
        productDTO2.setQuantity(100);
        productDTO2.setEnabled(true);

        InventoryDTO inventoryDTO2 = new InventoryDTO();
        inventoryDTO2.setProduct(productDTO1);
        inventoryDTO2.setQuantity(50);

        Flux<InventoryDTO> inventoryChanges = Flux.just(inventoryDTO1, inventoryDTO2);
        Flux<ProductDTO> productChanges = Flux.just(productDTO1, productDTO2);

        when(inventoryAddToManyProductsUseCase.apply(inventoryChanges)).thenReturn(productChanges);

        webTestClient.post()
            .uri("/product/inventory/add_many")
            .bodyValue(inventoryChanges)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(ProductDTO.class)
            .isEqualTo(List.of(productDTO1, productDTO2));
    }
}

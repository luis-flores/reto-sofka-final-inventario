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
        String id1 = "<product-id1>";
        int quantityBefore1 = 10;
        int change1 = 5;
        int quantityAfter1 = quantityBefore1 + change1;
        productDTO1.setId(id1);
        productDTO1.setQuantity(quantityBefore1);
        productDTO1.setEnabled(true);

        InventoryDTO inventoryDTO1 = new InventoryDTO();
        inventoryDTO1.setProduct(productDTO1);
        inventoryDTO1.setQuantity(change1);

        ProductDTO productDTO2 = new ProductDTO();
        String id2 = "<product-id2>";
        int quantityBefore2 = 100;
        int change2 = 50;
        int quantityAfter2 = quantityBefore2 + change2;
        productDTO2.setId(id2);
        productDTO2.setQuantity(quantityBefore2);
        productDTO2.setEnabled(true);

        InventoryDTO inventoryDTO2 = new InventoryDTO();
        inventoryDTO2.setProduct(productDTO2);
        inventoryDTO2.setQuantity(change2);

        List<InventoryDTO> inventoryChanges = List.of(inventoryDTO1, inventoryDTO2);
        ProductDTO productDTOResult1 = new ProductDTO();
        productDTOResult1.setId(id1);
        productDTOResult1.setQuantity(quantityAfter1);
        ProductDTO productDTOResult2 = new ProductDTO();
        productDTOResult2.setId(id2);
        productDTOResult2.setQuantity(quantityAfter2);
        List<ProductDTO> productResults = List.of(productDTOResult1, productDTOResult2);

        when(inventoryAddPerProductUseCase.apply(inventoryDTO1)).thenReturn(Mono.just(productDTOResult1));
        when(inventoryAddPerProductUseCase.apply(inventoryDTO2)).thenReturn(Mono.just(productDTOResult2));

        webTestClient.post()
            .uri("/product/inventory/add_many")
            .bodyValue(inventoryChanges)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(ProductDTO.class)
            .isEqualTo(productResults);
    }
}

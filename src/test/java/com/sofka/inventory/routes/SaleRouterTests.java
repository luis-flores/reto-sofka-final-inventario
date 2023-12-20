package com.sofka.inventory.routes;

import com.sofka.inventory.handlers.SaleHandler;
import com.sofka.inventory.models.dto.InventoryDTO;
import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.models.mongo.Product;
import com.sofka.inventory.useCases.SaleAddRetailUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
        saleHandler = new SaleHandler(saleAddRetailUseCase);
        saleRouter = new SaleRouter(saleHandler);
        webTestClient = WebTestClient.bindToRouterFunction(saleRouter.saleRoutes())
            .build();
    }

    @Test
    void SaleAddRetailRouteTest() {
        ProductDTO productDTO = new ProductDTO();
        String id = "<product-id>";
        productDTO.setId(id);
        int quantityBefore = 100;
        productDTO.setQuantity(quantityBefore);
        productDTO.setEnabled(true);

        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setProduct(productDTO);
        int saleQuantity = 7;
        inventoryDTO.setQuantity(saleQuantity);

        Flux<InventoryDTO> data = Flux.just(inventoryDTO);

        ProductDTO resultProduct = new ProductDTO();
        resultProduct.setId(id);
        int quantityAfter = quantityBefore - saleQuantity;
        resultProduct.setQuantity(quantityAfter);
        Flux<ProductDTO> result = Flux.just(resultProduct);

        when(saleAddRetailUseCase.apply(any())).thenReturn(result);

        webTestClient.post()
            .uri("/sale/retail")
            .contentType(MediaType.APPLICATION_JSON)
            .body(data, InventoryDTO.class)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(ProductDTO.class)
            .isEqualTo(List.of(resultProduct));
    }
}

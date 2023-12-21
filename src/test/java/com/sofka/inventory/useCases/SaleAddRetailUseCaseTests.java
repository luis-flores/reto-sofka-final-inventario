package com.sofka.inventory.useCases;

import com.sofka.inventory.drivenAdapters.bus.RabbitPublisher;
import com.sofka.inventory.models.dto.InventoryDTO;
import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.models.exceptions.ApplicationException;
import com.sofka.inventory.models.mongo.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SaleAddRetailUseCaseTests {
    private SaleAddRetailUseCase saleAddRetailUseCase;
    @Mock
    private ReactiveMongoTemplate mongoTemplate;
    @Mock
    private RabbitPublisher eventBus;
    private ModelMapper modelMapper;

    @BeforeEach
    public void setup() {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
            .setFieldMatchingEnabled(true)
            .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);

        saleAddRetailUseCase = new SaleAddRetailUseCase(mongoTemplate, modelMapper, eventBus);
    }

    @Test
    public void saleAddRetailUseCaseTest() {
        int quantityBefore = 10;
        int change = 5;
        int quantityAfter = quantityBefore - change;
        String id = "<product-id>";

        Product product = new Product();
        product.setId(id);
        product.setQuantity(quantityBefore);
        product.setEnabled(true);
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        Product productAfter = new Product();
        productAfter.setId(id);
        productAfter.setQuantity(quantityAfter);

        InventoryDTO inventoryDTO = new InventoryDTO(productDTO, change);

        when(mongoTemplate.findById(id, Product.class)).thenReturn(Mono.just(product));
        when(mongoTemplate.save(product)).thenReturn(Mono.just(productAfter));

        StepVerifier.create(saleAddRetailUseCase.apply(Flux.just(inventoryDTO)))
            .consumeNextWith(productResult -> {
                String resultId = productResult.getId();
                int resultQuantity = productResult.getQuantity();
                assert resultId.equals(id) : "Invalid id: " + resultId;
                assert productResult.getQuantity() == quantityAfter : "Invalid quantity: " + resultQuantity;
            })
            .expectComplete()
            .verify();
    }

    @Test
    public void saleAddRetail_ProductDoesNotExist() {
        Product product = new Product();
        product.setId("<product-id>");
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        InventoryDTO inventoryDTO = new InventoryDTO(productDTO, 1);

        when(mongoTemplate.findById(any(String.class), eq(Product.class))).thenReturn(Mono.empty());

        StepVerifier.create(saleAddRetailUseCase.apply(Flux.just(inventoryDTO)))
            .expectError(ApplicationException.class)
            .verify();
    }

    @Test
    public void saleAddRetail_ProductIsDisabled() {
        Product product = new Product();
        product.setId("<product-id>");
        product.setQuantity(100);
        product.setEnabled(false);
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        InventoryDTO inventoryDTO = new InventoryDTO(productDTO, 1);

        when(mongoTemplate.findById(any(String.class), eq(Product.class))).thenReturn(Mono.just(product));

        StepVerifier.create(saleAddRetailUseCase.apply(Flux.just(inventoryDTO)))
            .expectError(ApplicationException.class)
            .verify();
    }

    @Test
    public void saleAddRetail_QuantityLowerThanMinimum() {
        Product product = new Product();
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        InventoryDTO inventoryDTO = new InventoryDTO(productDTO, 0);

        StepVerifier.create(saleAddRetailUseCase.apply(Flux.just(inventoryDTO)))
            .expectError(ApplicationException.class)
            .verify();
    }

    @Test
    public void saleAddRetail_QuantityGreaterThanInventory() {
        Product product = new Product();
        product.setId("<product-id>");
        product.setQuantity(100);
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        InventoryDTO inventoryDTO = new InventoryDTO(productDTO, 5000);

        when(mongoTemplate.findById(any(String.class), eq(Product.class))).thenReturn(Mono.just(product));

        StepVerifier.create(saleAddRetailUseCase.apply(Flux.just(inventoryDTO)))
            .expectError(ApplicationException.class)
            .verify();
    }
}

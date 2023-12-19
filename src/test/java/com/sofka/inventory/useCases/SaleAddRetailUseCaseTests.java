package com.sofka.inventory.useCases;

import com.sofka.inventory.models.dto.InventoryDTO;
import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.models.exceptions.ProductNotFoundException;
import com.sofka.inventory.models.mongo.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SaleAddRetailUseCaseTests {
    private SaleAddRetailUseCase saleAddRetailUseCase;
    @Mock
    private ReactiveMongoTemplate mongoTemplate;
    private ModelMapper modelMapper;

    @BeforeEach
    public void setup() {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
            .setFieldMatchingEnabled(true)
            .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);

        saleAddRetailUseCase = new SaleAddRetailUseCase(mongoTemplate, modelMapper);
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

        InventoryDTO inventoryDTO = new InventoryDTO(productDTO, change);

        when(mongoTemplate.findById(id, Product.class)).thenReturn(Mono.just(product));
        when(mongoTemplate.save(product)).thenReturn(Mono.just(product));

        StepVerifier.create(saleAddRetailUseCase.apply(List.of(inventoryDTO)))
            .consumeNextWith(productResult -> {
                assert productResult.getId().equals(id);
                assert productResult.getQuantity() == quantityAfter;
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

        StepVerifier.create(saleAddRetailUseCase.apply(inventoryDTO))
            .expectError(ProductNotFoundException.class)
            .verify();
    }

    @Test
    public void saleAddRetail_ProductIsDisabled() {
        Product product = new Product();
        product.setId("<product-id>");
        product.setEnabled(false);
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        InventoryDTO inventoryDTO = new InventoryDTO(productDTO, 1);

        when(mongoTemplate.findById(any(String.class), eq(Product.class))).thenReturn(Mono.just(product));

        StepVerifier.create(saleAddRetailUseCase.apply(List.of(inventoryDTO)))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    public void saleAddRetail_QuantityLowerThanMinimum() {
        Product product = new Product();
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        InventoryDTO inventoryDTO = new InventoryDTO(productDTO, 0);

        StepVerifier.create(saleAddRetailUseCase.apply(List.of(inventoryDTO)))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    public void saleAddRetail_QuantityGreaterThanInventory() {
        Product product = new Product();
        product.setId("<product-id>");
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        InventoryDTO inventoryDTO = new InventoryDTO(productDTO, 5000);

        when(mongoTemplate.findById(any(String.class), eq(Product.class))).thenReturn(Mono.just(product));

        StepVerifier.create(saleAddRetailUseCase.apply(List.of(inventoryDTO)))
            .expectError(IllegalArgumentException.class)
            .verify();
    }
}

package com.sofka.inventory.useCases;

import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.models.mongo.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductAddInventoryPerUnitUseCaseTests {
    private ProductAddInventoryPerUnitUseCase productAddInventoryPerUnitUseCase;
    @Mock
    private ReactiveMongoTemplate mongoTemplate;
    private ModelMapper modelMapper;

    @BeforeEach
    public void setup() {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
            .setFieldMatchingEnabled(true)
            .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);

        productAddInventoryPerUnitUseCase = new ProductAddInventoryPerUnitUseCase(mongoTemplate, modelMapper);
    }

    @Test
    public void productAddInventoryPerUnitUseCaseTest() {
        int quantityBefore = 10;
        int change = 5;
        int quantityAfter = quantityBefore + change;

        Product product = new Product();
        product.setQuantity(quantityBefore);
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        InventoryDTO inventoryDTO = new InventoryDTO(productDTO, change);

        when(mongoTemplate.save(product)).thenReturn(Mono.just(product));

        StepVerifier.create(productAddInventoryPerUnitUseCase.apply(inventoryDTO))
            .expectNext(productDTO)
            .verifyComplete();
    }

    @Test
    public void productAddInventoryPerUnitProductDoesNotExist() {
        Product product = new Product();
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        InventoryDTO inventoryDTO = new InventoryDTO(productDTO, 0);

        when(mongoTemplate.findById(any(String.class), Product.class)).thenReturn(Mono.empty());

        StepVerifier.create(productAddInventoryPerUnitUseCase.apply(inventoryDTO))
            .expectError(EmptyResultDataAccessException.class)
            .verify();
    }

    @Test
    public void productAddInventoryPerUnitProductIsDisabled() {
        Product product = new Product();
        product.setEnabled(false);
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        InventoryDTO inventoryDTO = new InventoryDTO(productDTO, 0);

        when(mongoTemplate.findById(any(String.class), Product.class)).thenReturn(Mono.just(product));

        StepVerifier.create(productAddInventoryPerUnitUseCase.apply(inventoryDTO))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    public void productAddInventoryPerUnitQuantityLowerThanMinimum() {
        Product product = new Product();
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        InventoryDTO inventoryDTO = new InventoryDTO(productDTO, 0);
        
        StepVerifier.create(productAddInventoryPerUnitUseCase.apply(inventoryDTO))
            .expectError(IllegalArgumentException.class)
            .verify();
    }
}

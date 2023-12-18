package com.sofka.inventory.useCases;

import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.models.mongo.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductListUseCaseTests {
    private ProductListUseCase productListUseCase;
    @Mock
    private ReactiveMongoTemplate mongoTemplate;
    private ModelMapper modelMapper;
    @BeforeEach
    public void setup() {

    }

    @Test
    void listProductsTest() {
        Product product = new Product();
        when(mongoTemplate.findAll(Product.class)).thenReturn(Flux.just(product));

        StepVerifier.create(productListUseCase.get())
            .expectNext(modelMapper.map(product, ProductDTO.class))
            .verifyComplete();
    }
}

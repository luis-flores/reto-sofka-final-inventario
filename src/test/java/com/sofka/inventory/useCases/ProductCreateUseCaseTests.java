package com.sofka.inventory.useCases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
public class ProductCreateUseCaseTests {
    private ProductCreateUseCase productCreateUseCase;
    @Mock
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setup() {
        productCreateUseCase = new ProductCreateUseCase(mongoTemplate);
    }

    @Test
    public void createProductUseCaseTest() {
        Product product = new Product();
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        when(mongoTemplate.save(product)).thenReturn(Mono.just(product));

        StepVerifier.create(productCreateUseCase.apply(productDTO))
            .expectNext(productDTO)
            .verifyComplete();
    }
}

package com.sofka.inventory.useCases;

import com.sofka.inventory.drivenAdapters.bus.RabbitPublisher;
import com.sofka.inventory.models.dto.ProductDTO;
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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductCreateUseCaseTests {
    private ProductCreateUseCase productCreateUseCase;
    @Mock
    private ReactiveMongoTemplate mongoTemplate;
    private ModelMapper modelMapper;
    @Mock
    private RabbitPublisher eventBus;

    @BeforeEach
    public void setup() {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
            .setFieldMatchingEnabled(true)
            .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);

        productCreateUseCase = new ProductCreateUseCase(mongoTemplate, modelMapper, eventBus);
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

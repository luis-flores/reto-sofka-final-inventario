package com.sofka.inventory.useCases;

import com.sofka.inventory.drivenAdapters.bus.RabbitPublisher;
import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.models.mongo.Product;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Service
@Validated
@AllArgsConstructor
public class ProductCreateUseCase implements Function<ProductDTO, Mono<ProductDTO>> {
    private ReactiveMongoTemplate mongoTemplate;
    private ModelMapper modelMapper;
    private RabbitPublisher eventBus;

    @Override
    public Mono<ProductDTO> apply(ProductDTO productDTO) {
        Product product = modelMapper.map(productDTO, Product.class);

        return mongoTemplate.save(product)
            .doOnError(error -> eventBus.publishError("Save Product in Product Creation: ", error.getMessage()))
            .doOnSuccess(success -> {
                eventBus.publishRecord("Product Created: ", product);
                eventBus.publishProductMovement("Product Created: ", product);
            })
            .map(productModel -> modelMapper.map(productModel, ProductDTO.class));
    }
}

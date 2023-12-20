package com.sofka.inventory.useCases;

import com.sofka.inventory.drivenAdapters.bus.RabbitPublisher;
import com.sofka.inventory.models.dto.InventoryDTO;
import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.models.exceptions.ProductNotFoundException;
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
public class InventoryAddPerProductUseCase implements Function<InventoryDTO, Mono<ProductDTO>> {
    private ReactiveMongoTemplate mongoTemplate;
    private ModelMapper modelMapper;
    private RabbitPublisher eventBus;

    @Override
    public Mono<ProductDTO> apply(InventoryDTO inventoryDTO) throws IllegalArgumentException {
        int minimum = 1;
        int quantityToAdd = inventoryDTO.getQuantity();
        if (quantityToAdd < minimum) {
            var error = new IllegalArgumentException(
                String.format("Quantity must be greater than %d", minimum)
            );
            eventBus.publishError("Validation Error in Add Product Inventory: ", error);
            return Mono.error(error);
        }

        ProductDTO productDTO = inventoryDTO.getProduct();
        String id = productDTO.getId();

        return mongoTemplate.findById(id, Product.class)
            .switchIfEmpty(
                Mono.error(
                    new ProductNotFoundException(
                        String.format("Product %s not found", id)
                    )
                )
            )
            .doOnError(error -> eventBus.publishError("Product Error in Add Product Inventory: ", error))
            .flatMap(productFound -> {
                if (productFound.getEnabled() == false) {
                    var error = new IllegalArgumentException(
                        String.format("Product %s is not enabled", id)
                    );
                    eventBus.publishError("Validation Error in Add Product Inventory: ", error);
                    return Mono.error(error);
                } else {
                    int currentQuantity = productFound.getQuantity();
                    productFound.setQuantity(currentQuantity + quantityToAdd);

                    return mongoTemplate.save(productFound)
                        .doOnError(error -> eventBus.publishError("Save Error in Add Product Inventory: ", error))
                        .doOnSuccess(success -> {
                            eventBus.publishRecord("Add Product Inventory Successful: ", inventoryDTO);
                            eventBus.publishProductMovement("Add Product Inventory Successful: ", inventoryDTO);
                        })
                        .map(productSaved -> modelMapper.map(productSaved, ProductDTO.class));
                }
            });
    }
}

package com.sofka.inventory.useCases;

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

    @Override
    public Mono<ProductDTO> apply(InventoryDTO inventoryDTO) throws IllegalArgumentException {
        int minimum = 1;
        int quantityToAdd = inventoryDTO.getQuantity();
        if (quantityToAdd < minimum)
            return Mono.error(new IllegalArgumentException("Quantity must be greater than " + minimum));

        ProductDTO productDTO = inventoryDTO.getProduct();
        String id = productDTO.getId();

        return mongoTemplate.findById(id, Product.class)
            .switchIfEmpty(Mono.error(new ProductNotFoundException("Product " + id + " not found")))
            .flatMap(productFound -> {
                if (productFound.getEnabled() == false) {
                    return Mono.error(new IllegalArgumentException("Product " + id + " is not enabled"));
                } else {
                    int currentQuantity = productFound.getQuantity();
                    productFound.setQuantity(currentQuantity + quantityToAdd);

                    return mongoTemplate.save(productFound)
                        .map(productSaved -> modelMapper.map(productSaved, ProductDTO.class));
                }
            });
    }
}

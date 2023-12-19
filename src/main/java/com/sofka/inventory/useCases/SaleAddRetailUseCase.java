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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Service
@Validated
@AllArgsConstructor
public class SaleAddRetailUseCase implements Function<Flux<InventoryDTO>, Flux<ProductDTO>> {
    private ReactiveMongoTemplate mongoTemplate;
    private ModelMapper modelMapper;

    @Override
    public Flux<ProductDTO> apply(Flux<InventoryDTO> inventoryDTOs) {
        return inventoryDTOs
            .flatMap(this::checkQuantityMinimum)
            .flatMap(this::getProduct)
            .flatMap(this::checkSaleQuantity)
            .flatMap(this::checkProductDisabled)
            .flatMap(this::changeProduct);
    }

    private Mono<InventoryDTO> checkQuantityMinimum(InventoryDTO inventoryDTO) {
        int minimum = 1;

        ProductDTO productDTO = inventoryDTO.getProduct();
        int saleQuantity = inventoryDTO.getQuantity();

        if (saleQuantity < minimum) {
            return Mono.error(
                new IllegalArgumentException(
                    "Product " + productDTO.getId() +
                        " with quantity " + productDTO.getQuantity() +
                        " lower than minimum: " + minimum
                )
            );
        }

        return Mono.just(inventoryDTO);
    }

    private Mono<InventoryDTO> getProduct(InventoryDTO inventoryDTO) {
        ProductDTO productDTO = inventoryDTO.getProduct();
        String id = productDTO.getId();

        return mongoTemplate.findById(id, Product.class)
            .switchIfEmpty(Mono.error(new ProductNotFoundException("Product " + id + " not found")))
            .map(productFound -> {
                inventoryDTO.setProduct(modelMapper.map(productFound, ProductDTO.class));
                return inventoryDTO;
            });
    }

    private Mono<InventoryDTO> checkSaleQuantity(InventoryDTO inventoryDTO) {
        ProductDTO productDTO = inventoryDTO.getProduct();
        int saleQuantity = inventoryDTO.getQuantity();
        int currentQuantity = productDTO.getQuantity();

        if (currentQuantity < saleQuantity) {
            return Mono.error(
                new IllegalArgumentException(
                    "Product " + productDTO.getId() +
                        " has quantity " + currentQuantity +
                        " lower than requested in sale: " + saleQuantity
                )
            );
        }

        return Mono.just(inventoryDTO);
    }

    private Mono<InventoryDTO> checkProductDisabled(InventoryDTO inventoryDTO) {
        ProductDTO productDTO = inventoryDTO.getProduct();

        if (productDTO.getEnabled() == false) {
            return Mono.error(
                new IllegalArgumentException(
                    "Product " + productDTO.getId() + " is disabled"
                )
            );
        }

        return Mono.just(inventoryDTO);
    }

    private Mono<ProductDTO> changeProduct(InventoryDTO inventoryDTO) {
        ProductDTO productDTO = inventoryDTO.getProduct();
        Product product = modelMapper.map(productDTO, Product.class);
        product.setQuantity(product.getQuantity() - inventoryDTO.getQuantity());

        return mongoTemplate.save(product)
            .map(productSave -> modelMapper.map(productSave, ProductDTO.class));
    }
}

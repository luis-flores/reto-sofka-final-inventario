package com.sofka.inventory.useCases;

import com.sofka.inventory.drivenAdapters.bus.RabbitPublisher;
import com.sofka.inventory.models.dto.InventoryDTO;
import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.models.exceptions.ApplicationException;
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
    private RabbitPublisher eventBus;

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
            var error = new ApplicationException(
                String.format("Product %s with quantity %d lower than minimum: %d",
                    productDTO.getId(), productDTO.getQuantity(), minimum)
            );
            eventBus.publishError("Validation Error in Retail Sale: ", error.getMessage());
            return Mono.error(error);
        }

        return Mono.just(inventoryDTO);
    }

    private Mono<InventoryDTO> getProduct(InventoryDTO inventoryDTO) {
        ProductDTO productDTO = inventoryDTO.getProduct();
        String id = productDTO.getId();

        return mongoTemplate.findById(id, Product.class)
            .switchIfEmpty(
                Mono.defer(() -> {
                    var error = new ApplicationException(
                        String.format("Product %s not found", id)
                    );
                    eventBus.publishError("Product Error in Retail Sale: ", error.getMessage());
                    return Mono.error(error);
                })
            )
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
            var error = new ApplicationException(
                String.format("Product %s has quantity %d lower than quantity requested in sale: %d",
                    productDTO.getId(), currentQuantity, saleQuantity)
            );
            eventBus.publishError("Validation Error in Retail Sale: ", error.getMessage());
            return Mono.error(error);
        }

        return Mono.just(inventoryDTO);
    }

    private Mono<InventoryDTO> checkProductDisabled(InventoryDTO inventoryDTO) {
        ProductDTO productDTO = inventoryDTO.getProduct();

        if (productDTO.getEnabled() == false) {
            var error = new ApplicationException(
                String.format("Product %s is disabled", productDTO.getId())
            );
            eventBus.publishError("Validation Error in Retail Sale: ", error.getMessage());
            return Mono.error(error);
        }

        return Mono.just(inventoryDTO);
    }

    private Mono<ProductDTO> changeProduct(InventoryDTO inventoryDTO) {
        ProductDTO productDTO = inventoryDTO.getProduct();
        Product product = modelMapper.map(productDTO, Product.class);
        product.setQuantity(product.getQuantity() - inventoryDTO.getQuantity());

        return mongoTemplate.save(product)
            .doOnError(error -> eventBus.publishError("Save Error in Retail Sale: ", error.getMessage()))
            .doOnSuccess(success -> {
                eventBus.publishRetailSale("Retail Sale Recorded: ", inventoryDTO);
                eventBus.publishProductMovement("Product Sold in Retail Sale: ", inventoryDTO);
            })
            .map(productSave -> modelMapper.map(productSave, ProductDTO.class));
    }
}

package com.sofka.inventory.useCases;

import com.sofka.inventory.models.dto.ProductDTO;
import com.sofka.inventory.models.mongo.Product;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;

import java.util.function.IntFunction;

@Service
@Validated
@AllArgsConstructor
public class ProductListUseCase implements IntFunction<Flux<ProductDTO>> {
    private ReactiveMongoTemplate mongoTemplate;
    private ModelMapper modelMapper;

    @Override
    public Flux<ProductDTO> apply(int page) {
        int pageSize = 100;
        long skip = (long) page * pageSize;

        Query query = new Query()
            .skip(skip)
            .limit(pageSize);

        query.fields()
            .include("code")
            .include("quantity");

        return mongoTemplate.find(query, Product.class)
            .map(product -> modelMapper.map(product, ProductDTO.class));
    }
}

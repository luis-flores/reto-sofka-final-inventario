package com.sofka.inventory;

import com.google.gson.Gson;
import com.rabbitmq.client.Connection;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import jakarta.annotation.PreDestroy;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableWebFlux
@OpenAPIDefinition
public class InventoryApplication {
    @Autowired
    private Mono<Connection> connectionMono;

    @Bean
    public Gson createGson() {
        return new Gson();
    }

	public static void main(String[] args) {
		SpringApplication.run(InventoryApplication.class, args);
	}

    @PreDestroy
    public void close() throws Exception {
        connectionMono.block().close();
    }
}

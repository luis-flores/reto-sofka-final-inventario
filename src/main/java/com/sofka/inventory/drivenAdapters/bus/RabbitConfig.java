package com.sofka.inventory.drivenAdapters.bus;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class RabbitConfig {
    public static final String RECORD_QUEUE = "inv-records-queue";
    public static final String ERROR_QUEUE = "inv-errors-queue";
    public static final String PRODUCT_MOVEMENTS_QUEUE = "inv-product-movs-queue";
    public static final String RETAIL_SALES_QUEUE = "inv-retail-queue";
    public static final String WHOLESALE_QUEUE = "inv-wholesale-queue";

    public static final String RECORD_ROUTING_KEY = "inv.records.rk";
    public static final String ERROR_ROUTING_KEY = "inv.errors.rk";
    public static final String PRODUCT_ROUTING_KEY = "inv.product.rk";
    public static final String RETAIL_ROUTING_KEY = "inv.retail.rk";
    public static final String WHOLESALE_ROUTING_KEY = "inv.wholesale.rk";

    public static final String EXCHANGE_NAME = "inventory-exchange";

    @Bean
    public static URI uri(@Value("${rabbit.uri}") String uri) {
        return URI.create(uri);
    }
    @Bean
    public AmqpAdmin amqpAdmin(URI uri) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(uri);
        var amqpAdmin =  new RabbitAdmin(connectionFactory);

        var exchange = new TopicExchange(EXCHANGE_NAME);

        var recordQueue = new Queue(RECORD_QUEUE, true, false, false);
        var errorQueue = new Queue(ERROR_QUEUE, true, false, false);
        var productQueue = new Queue(PRODUCT_MOVEMENTS_QUEUE, true, false, false);
        var retailQueue = new Queue(RETAIL_SALES_QUEUE, true, false, false);
        var wholesaleQueue = new Queue(WHOLESALE_QUEUE, true, false, false);

        amqpAdmin.declareExchange(exchange);

        amqpAdmin.declareQueue(recordQueue);
        amqpAdmin.declareQueue(errorQueue);
        amqpAdmin.declareQueue(productQueue);
        amqpAdmin.declareQueue(retailQueue);
        amqpAdmin.declareQueue(wholesaleQueue);

        amqpAdmin.declareBinding(BindingBuilder.bind(recordQueue).to(exchange).with(RECORD_ROUTING_KEY));
        amqpAdmin.declareBinding(BindingBuilder.bind(errorQueue).to(exchange).with(ERROR_ROUTING_KEY));
        amqpAdmin.declareBinding(BindingBuilder.bind(productQueue).to(exchange).with(PRODUCT_ROUTING_KEY));
        amqpAdmin.declareBinding(BindingBuilder.bind(retailQueue).to(exchange).with(RETAIL_ROUTING_KEY));
        amqpAdmin.declareBinding(BindingBuilder.bind(wholesaleQueue).to(exchange).with(WHOLESALE_ROUTING_KEY));

        return amqpAdmin;
    }

    @Bean
    public ConnectionFactory connectionFactory(URI uri) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.useNio();
        connectionFactory.setUri(uri);
        return connectionFactory;
    }

    @Bean
    public Mono<Connection> connectionMono(@Value("spring.application.name") String name, ConnectionFactory connectionFactory)  {
        return Mono.fromCallable(() -> connectionFactory.newConnection(name)).cache();
    }

    @Bean
    public SenderOptions senderOptions(Mono<Connection> connectionMono) {
        return new SenderOptions()
            .connectionMono(connectionMono)
            .resourceManagementScheduler(Schedulers.boundedElastic());
    }

    @Bean
    public Sender sender(SenderOptions senderOptions) {
        return RabbitFlux.createSender(senderOptions);
    }
}

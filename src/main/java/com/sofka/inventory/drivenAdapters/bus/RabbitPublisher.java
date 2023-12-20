package com.sofka.inventory.drivenAdapters.bus;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@Component
public class RabbitPublisher {
    @Autowired
    private Sender sender;

    @Autowired
    private Gson gson;

    public void publishRecord(String message, Object object){
        publishLog(message, object, RabbitConfig.RECORD_ROUTING_KEY);
    }

    public void publishError(String message, Object error) {
        publishLog(message, error, RabbitConfig.ERROR_ROUTING_KEY);
    }

    public void publishProductMovement(String message, Object object) {
        publishLog(message, object, RabbitConfig.PRODUCT_ROUTING_KEY);
    }

    public void publishRetailSale(String message, Object object) {
        publishLog(message, object, RabbitConfig.RETAIL_ROUTING_KEY);
    }

    public void publishWholesale(String message, Object object) {
        publishLog(message, object, RabbitConfig.WHOLESALE_ROUTING_KEY);
    }

    private void publishLog(String message, Object object, String routingKey){
        String logMessage = "";

        if (!message.isEmpty())
            logMessage += message;
        if (object != null)
            logMessage += gson.toJson(object);

        sender.send(
            Mono.just(
                new OutboundMessage(
                    RabbitConfig.EXCHANGE_NAME,
                    routingKey,
                    logMessage.getBytes()
                )
            )
        ).subscribe();
    }
}

package com.rodrigolopes.inventory_service.producers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class ProducerService {


    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;
    public ProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public void sendMessage(String topic, Object message) {
        try{
            var event = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(topic, event);
        }catch (Exception e){
            log.warn(e.getMessage());
        }

    }
}

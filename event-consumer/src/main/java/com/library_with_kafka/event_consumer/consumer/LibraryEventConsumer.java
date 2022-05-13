package com.library_with_kafka.event_consumer.consumer;

import com.library_with_kafka.event_consumer.service.LibraryEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryEventConsumer {

    private final LibraryEventService service;

    @KafkaListener(topics = {"library-events"}, concurrency = "3")
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) {
        log.info("Consumer record: {}", consumerRecord);
        service.processKafkaRecord(consumerRecord);
    }

}

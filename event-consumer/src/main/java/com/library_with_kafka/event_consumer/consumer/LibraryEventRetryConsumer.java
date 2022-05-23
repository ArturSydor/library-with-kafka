package com.library_with_kafka.event_consumer.consumer;

import com.library_with_kafka.event_consumer.constant.KafkaTopic;
import com.library_with_kafka.event_consumer.service.LibraryEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventRetryConsumer {

    private final LibraryEventService eventService;

    @KafkaListener(topics = {KafkaTopic.LIBRARY_EVENTS_RETRY},
                    autoStartup = "${retryListener.library.active:true}",
                    groupId = "retry-listener-group")
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) {
        log.info("Retry ConsumerRecord: {}", consumerRecord);
        consumerRecord.headers().forEach(header -> {
            log.info("Key: {}, Value: {}", header.key(), new String(header.value()));
        });
        eventService.processKafkaRecordRetry(consumerRecord);
    }

}

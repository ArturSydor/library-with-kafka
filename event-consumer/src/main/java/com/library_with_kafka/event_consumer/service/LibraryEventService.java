package com.library_with_kafka.event_consumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library_with_kafka.event_consumer.domain.event.LibraryEvent;
import com.library_with_kafka.event_consumer.domain.event.LibraryEventType;
import com.library_with_kafka.event_consumer.exception.RecoverableException;
import com.library_with_kafka.event_consumer.repository.LibraryEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryEventService {

    private final LibraryEventRepository eventRepository;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void processKafkaRecord(ConsumerRecord<Integer, String> consumerRecord) {
        Objects.requireNonNull(consumerRecord.value());
        var event = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        validateEvent(event);

        event.getBook().setLibraryEvent(event);

        var savedEvent = eventRepository.save(event);

        log.info("Saved event: {}", savedEvent);
    }

    @SneakyThrows
    public void processKafkaRecordRetry(ConsumerRecord<Integer, String> consumerRecord) {
        Objects.requireNonNull(consumerRecord.value());
        var event = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);

        if (Objects.nonNull(event.getId()) && event.getId() == -1) {
            throw new IllegalStateException("Something is wrong with id");
        }

        event.getBook().setLibraryEvent(event);

        var savedEvent = eventRepository.save(event);

        log.info("Saved event: {}", savedEvent);
    }

    private void validateEvent(LibraryEvent event) {
        if (LibraryEventType.UPDATE.equals(event.getEventType()) && Objects.isNull(event.getId())) {
            log.error("Updating event without ID is forbidden, Event: {}", event);
            throw new IllegalStateException("Updating event without ID is forbidden!");
        }

        if (Objects.nonNull(event.getId()) && event.getId() == -1) {
            log.error("New record will be published to retry topic");
            throw new RecoverableException("Something is wrong with id");
        }
    }

}

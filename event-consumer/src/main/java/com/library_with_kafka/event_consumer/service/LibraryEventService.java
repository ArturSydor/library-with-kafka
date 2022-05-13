package com.library_with_kafka.event_consumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library_with_kafka.event_consumer.domain.event.LibraryEvent;
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

        event.getBook().setLibraryEvent(event);

        var savedEvent= eventRepository.save(event);

        log.info("Saved event: {}", savedEvent);
    }

}

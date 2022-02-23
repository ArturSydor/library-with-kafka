package com.library_with_kafka.event_producer.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library_with_kafka.event_producer.domain.LibraryEvent;
import com.library_with_kafka.event_producer.utilities.JsonUtilities;
import com.library_with_kafka.event_producer.utilities.ProducerCallbackUtilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public record LibraryEventProducer(
        KafkaTemplate<Integer, String> kafkaTemplate,
        ObjectMapper objectMapper) {

    public void process(LibraryEvent libraryEvent, boolean isAsync) {
        log.debug("Started processing book - {}", libraryEvent.getBook().name());
        var futureResult = kafkaTemplate.send(getMessage(libraryEvent));

        if (isAsync) {
            futureResult.addCallback(ProducerCallbackUtilities::handleSuccess,
                    ex -> ProducerCallbackUtilities.handleFailure(ex, libraryEvent));
        } else {
            try {
                ProducerCallbackUtilities.handleSuccess(futureResult.get());
            } catch (Exception e) {
                ProducerCallbackUtilities.handleFailure(e, libraryEvent);
            }
        }

        log.debug("Finished processing book - {}", libraryEvent.getBook().name());
    }


    private ProducerRecord<Integer, String> getMessage(LibraryEvent event) {
        List<Header> headers = List.of(new RecordHeader("event-source", "qr-scanner".getBytes()));

        return new ProducerRecord<>(event.getTopic(), null, event.getKey(),
                JsonUtilities.getJsonString(objectMapper, event.getValue()), headers);
    }
}

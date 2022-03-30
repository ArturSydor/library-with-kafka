package com.library_with_kafka.event_producer.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library_with_kafka.event_producer.domain.event.LibraryEvent;
import com.library_with_kafka.event_producer.utilities.KafkaProducerUtilities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryEventProducer {

    private final KafkaTemplate<Integer, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public void process(LibraryEvent libraryEvent) {
        performSending(libraryEvent,
                futureResult -> {
                    try {
                        KafkaProducerUtilities.handleSuccess(futureResult.get());
                    } catch (Exception e) {
                        KafkaProducerUtilities.handleFailure(e, libraryEvent);
                    }
                });
    }

    public ListenableFuture<SendResult<Integer, String>> processAsync(LibraryEvent libraryEvent) {
        return performSending(libraryEvent,
                futureResult -> futureResult.addCallback(KafkaProducerUtilities::handleSuccess,
                ex -> KafkaProducerUtilities.handleFailure(ex, libraryEvent)));
    }

    private ListenableFuture<SendResult<Integer, String>> performSending(LibraryEvent libraryEvent, Consumer<ListenableFuture<SendResult<Integer, String>>> resultProcessor) {
        log.debug("Started processing book - {}", libraryEvent.value().getBook().name());
        var futureResult = kafkaTemplate.send(KafkaProducerUtilities.createRecordWithIntKey(libraryEvent, objectMapper));

        resultProcessor.accept(futureResult);

        log.debug("Finished processing book - {}", libraryEvent.value().getBook().name());

        return futureResult;
    }

}

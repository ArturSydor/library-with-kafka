package com.library_with_kafka.event_producer.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library_with_kafka.event_producer.domain.event.LibraryEvent;
import com.library_with_kafka.event_producer.utilities.JsonUtilities;
import com.library_with_kafka.event_producer.utilities.KafkaProducerUtilities;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.function.Consumer;

import static com.library_with_kafka.event_producer.data_factory.LibraryEventDataFactory.getDefaultCreatedLibraryEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryEventProducerTest {

    private static final LibraryEvent DEFAULT_EVENT = getDefaultCreatedLibraryEvent();

    private static final int DEFAULT_PARTITION = 1;

    @Mock
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    LibraryEventProducer libraryEventProducer;

    @Test
    void processOnFailure() {
        mockKafkaTemplateWithFailureResult();

        testWithMockedKafkaProducerUtilities(producerUtilitiesMocked -> {
            producerUtilitiesMocked.verify(
                    () -> KafkaProducerUtilities.handleSuccess(isA(SendResult.class)),
                    times(0));
            producerUtilitiesMocked.verify(
                    () -> KafkaProducerUtilities.handleFailure(isA(Exception.class), isA(LibraryEvent.class)),
                    times(1));
        });
    }

    @Test
    void processAsyncOnFailure() {
        mockKafkaTemplateWithFailureResult();

        assertThrows(Exception.class, () -> libraryEventProducer.processAsync(DEFAULT_EVENT).get());
    }

    @Test
    void processOnSuccess() {
        mockKafkaTemplateWithSuccessResult();

        testWithMockedKafkaProducerUtilities(producerUtilitiesMocked -> {
            producerUtilitiesMocked.verify(
                    () -> KafkaProducerUtilities.handleSuccess(isA(SendResult.class)),
                    times(1));
            producerUtilitiesMocked.verify(
                    () -> KafkaProducerUtilities.handleFailure(isA(Exception.class), isA(LibraryEvent.class)),
                    times(0));
        });
    }

    @Test
    @SneakyThrows
    void processAsyncOnSuccess() {
        mockKafkaTemplateWithSuccessResult();

        var actualResult = libraryEventProducer.processAsync(DEFAULT_EVENT).get();
        assertEquals(DEFAULT_PARTITION, actualResult.getRecordMetadata().partition());
        assertEquals(JsonUtilities.getJsonString(objectMapper, DEFAULT_EVENT.value()), actualResult.getProducerRecord().value());
    }

    private void mockKafkaTemplateWithFailureResult() {
        var failureResult = new SettableListenableFuture<>();
        failureResult.setException(new RuntimeException("Exception Calling Kafka"));

        doReturn(failureResult).when(kafkaTemplate).send(isA(ProducerRecord.class));
    }

    private void mockKafkaTemplateWithSuccessResult() {
        var producerRecord = KafkaProducerUtilities.createRecordWithIntKey(DEFAULT_EVENT, objectMapper);
        var recordMeta = new RecordMetadata(new TopicPartition(DEFAULT_EVENT.topic(), DEFAULT_PARTITION), 1, 1, 1, 1, 1);
        var sendResult = new SendResult<>(producerRecord, recordMeta);
        var successResult = new SettableListenableFuture<>();
        successResult.set(sendResult);

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(successResult);
    }

    private void testWithMockedKafkaProducerUtilities(Consumer<MockedStatic<KafkaProducerUtilities>> mockConsumer) {
        var producerRecord = KafkaProducerUtilities.createRecordWithIntKey(DEFAULT_EVENT, objectMapper);
        try (MockedStatic<KafkaProducerUtilities> producerUtilitiesMocked = Mockito.mockStatic(KafkaProducerUtilities.class)) {
            producerUtilitiesMocked
                    .when(() -> KafkaProducerUtilities.createRecordWithIntKey(isA(LibraryEvent.class), isA(ObjectMapper.class)))
                    .thenReturn(producerRecord);

            libraryEventProducer.process(DEFAULT_EVENT);

            mockConsumer.accept(producerUtilitiesMocked);
        }
    }

}
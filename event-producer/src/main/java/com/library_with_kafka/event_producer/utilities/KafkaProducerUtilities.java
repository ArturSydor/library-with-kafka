package com.library_with_kafka.event_producer.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library_with_kafka.event_producer.domain.event.KafkaEvent;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.Objects;

@Slf4j
@UtilityClass
public class KafkaProducerUtilities {

    public void handleSuccess(SendResult<Integer, String> result) {
        Objects.requireNonNull(result);
        var record = result.getProducerRecord();
        var metaData = result.getRecordMetadata();
        log.debug("Message with key=[{}] and value=[{}] was successfully delivered to partition=[{}] of topic=[{}]",
                record.key(), record.value(), metaData.partition(), metaData.topic());
    }

    public void handleFailure(Throwable ex, KafkaEvent<?, ?> event) {
        Objects.requireNonNull(event);
        log.error(String.format("Failed to deliver message with key=[%s] and value=[%s] to kafka topic=[%s].",
                event.key(), event.value(), event.topic()), ex);
    }

    public ProducerRecord<Integer, String> createRecordWithIntKey(KafkaEvent<Integer, ?> event, ObjectMapper objectMapper) {
        List<Header> headers = List.of(new RecordHeader("event-source", "qr-scanner".getBytes()));

        return new ProducerRecord<>(event.topic(), null, event.key(),
                JsonUtilities.getJsonString(objectMapper, event.value()), headers);
    }

}

package com.library_with_kafka.event_producer.utilities;

import com.library_with_kafka.event_producer.domain.KafkaEvent;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;

import java.util.Objects;

@Slf4j
@UtilityClass
public class ProducerCallbackUtilities {

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
                event.getKey(), event.getValue(), event.getTopic()), ex);
    }

}

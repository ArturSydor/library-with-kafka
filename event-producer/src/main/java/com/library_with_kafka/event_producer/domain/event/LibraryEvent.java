package com.library_with_kafka.event_producer.domain.event;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.library_with_kafka.event_producer.constant.KafkaTopic.LIBRARY_EVENTS;

public record LibraryEvent(Integer key,
                           @NotNull @Valid LibraryEventValue value) implements KafkaEvent<Integer, LibraryEventValue> {

    @Override
    public String topic() {
        return LIBRARY_EVENTS.getName();
    }

}

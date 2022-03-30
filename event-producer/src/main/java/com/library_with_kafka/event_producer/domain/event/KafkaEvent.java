package com.library_with_kafka.event_producer.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface KafkaEvent<K, V> {

    K key();

    V value();

    @JsonIgnore
    String topic();

}

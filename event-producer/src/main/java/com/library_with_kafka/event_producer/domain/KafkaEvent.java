package com.library_with_kafka.event_producer.domain;

public interface KafkaEvent<K, V> {

    K getKey();

    V getValue();

    String getTopic();

}

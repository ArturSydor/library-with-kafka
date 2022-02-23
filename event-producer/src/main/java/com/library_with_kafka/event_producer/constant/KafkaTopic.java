package com.library_with_kafka.event_producer.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum KafkaTopic {
    LIBRARY_EVENTS("library-events", 3, 1);

    private final String name;
    private final int partitions;
    private final int replication;
}

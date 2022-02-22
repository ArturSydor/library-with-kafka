package com.library_with_kafka.event_producer.domain;

import lombok.Builder;

@Builder
public record LibraryEvent(
        Long id,
        Book book
) {
}

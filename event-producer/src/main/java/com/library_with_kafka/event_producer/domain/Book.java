package com.library_with_kafka.event_producer.domain;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record Book(
        Long id,
        String name,
        BigDecimal price,
        Author author
) {
}

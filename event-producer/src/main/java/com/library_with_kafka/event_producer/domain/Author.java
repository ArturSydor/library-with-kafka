package com.library_with_kafka.event_producer.domain;

import lombok.Builder;

@Builder
public record Author(
        Integer id,
        String firstName,
        String lastName,
        String email
) {
}

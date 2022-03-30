package com.library_with_kafka.event_producer.domain;

import lombok.Builder;

import javax.validation.constraints.NotBlank;

@Builder
public record Author(
        Integer id,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String email
) {
}

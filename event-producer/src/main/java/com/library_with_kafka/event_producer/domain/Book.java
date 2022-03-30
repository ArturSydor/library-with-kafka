package com.library_with_kafka.event_producer.domain;

import lombok.Builder;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Builder
public record Book(
        Long id,
        @NotBlank String name,
        @NotNull BigDecimal price,
        @NotNull @Valid Author author
) {
}

package com.library_with_kafka.event_producer.domain.event;

import com.library_with_kafka.event_producer.domain.Book;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LibraryEventValue {

    @NotNull
    @Valid
    private Book book;

    private LibraryEventType eventType;

}

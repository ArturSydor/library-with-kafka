package com.library_with_kafka.event_consumer.domain.event;

import com.library_with_kafka.event_consumer.domain.Book;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class LibraryEvent {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(mappedBy = "libraryEvent", cascade = CascadeType.ALL)
    private Book book;

    @Enumerated(EnumType.STRING)
    private LibraryEventType eventType;

}

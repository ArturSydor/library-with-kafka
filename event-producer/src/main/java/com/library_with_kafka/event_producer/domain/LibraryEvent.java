package com.library_with_kafka.event_producer.domain;

import lombok.*;

import static com.library_with_kafka.event_producer.constant.KafkaTopic.LIBRARY_EVENTS;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryEvent implements KafkaEvent<Integer, Book> {

    private Integer id;

    private Book book;

    @Setter
    private LibraryEventType eventType;

    @Override
    public Integer getKey() {
        return this.id;
    }

    @Override
    public Book getValue() {
        return this.book;
    }

    @Override
    public String getTopic() {
        return LIBRARY_EVENTS.getName();
    }
}

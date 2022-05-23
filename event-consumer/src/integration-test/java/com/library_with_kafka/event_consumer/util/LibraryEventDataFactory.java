package com.library_with_kafka.event_consumer.util;


import com.library_with_kafka.event_consumer.domain.Author;
import com.library_with_kafka.event_consumer.domain.Book;
import com.library_with_kafka.event_consumer.domain.event.LibraryEvent;
import com.library_with_kafka.event_consumer.domain.event.LibraryEventType;

import java.math.BigDecimal;

public final class LibraryEventDataFactory {

    public static LibraryEvent getNewLibraryEvent() {
        var newEvent = new LibraryEvent();

        var author = new Author();
        author.setEmail("test@mail.com");
        author.setFirstName("test");
        author.setLastName("test");

        var book = new Book();
        book.setName("test");
        book.setPrice(BigDecimal.ONE);
        book.setAuthor(author);

        newEvent.setEventType(LibraryEventType.CREATE);
        newEvent.setBook(book);

        return newEvent;
    }

}

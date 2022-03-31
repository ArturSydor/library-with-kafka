package com.library_with_kafka.event_producer.data_factory;

import com.library_with_kafka.event_producer.domain.Author;
import com.library_with_kafka.event_producer.domain.Book;
import com.library_with_kafka.event_producer.domain.event.LibraryEvent;
import com.library_with_kafka.event_producer.domain.event.LibraryEventType;
import com.library_with_kafka.event_producer.domain.event.LibraryEventValue;

import java.math.BigDecimal;

public class LibraryEventDataFactory {

    public static LibraryEvent getDefaultNewLibraryEvent() {
        var author = new Author(null, "Test_FirstName", "Test_Surname", "test@mail.com");
        var newBook = new Book(null, "Test_Name", BigDecimal.TEN, author);
        var libraryEventValue = new LibraryEventValue(newBook, LibraryEventType.CREATE);
        return new LibraryEvent(null, libraryEventValue);
    }

    public static LibraryEvent getDefaultLibraryEventForUpdate() {
        var author = new Author(1, "Test_FirstName", "Test_Surname", "test@mail.com");
        var newBook = new Book(1L, "Test_Name", BigDecimal.TEN, author);
        var libraryEventValue = new LibraryEventValue(newBook, LibraryEventType.UPDATE);
        return new LibraryEvent(1, libraryEventValue);
    }

}

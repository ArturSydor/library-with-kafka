package com.library_with_kafka.event_producer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library_with_kafka.event_producer.domain.Author;
import com.library_with_kafka.event_producer.domain.Book;
import com.library_with_kafka.event_producer.domain.event.LibraryEvent;
import com.library_with_kafka.event_producer.domain.event.LibraryEventType;
import com.library_with_kafka.event_producer.domain.event.LibraryEventValue;
import com.library_with_kafka.event_producer.producer.LibraryEventProducer;
import com.library_with_kafka.event_producer.utilities.JsonUtilities;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static com.library_with_kafka.event_producer.data_factory.LibraryEventDataFactory.getDefaultCreatedLibraryEvent;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventController.class)
@AutoConfigureMockMvc
class LibraryEventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LibraryEventProducer libraryEventProducer;

    @Test
    @SneakyThrows
    void postLibraryEvent() {
        doNothing().when(libraryEventProducer).process(isA(LibraryEvent.class));

        mockMvc.perform(post("/v1/library/event")
                .content(JsonUtilities.getJsonString(objectMapper, getDefaultCreatedLibraryEvent()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void postLibraryEventBodyValidationFailed() {
        var author = new Author(null, null, "Surname", "test@mail.com");
        var newBook = new Book(null, null, BigDecimal.TEN, author);
        var libraryEventValue = new LibraryEventValue(newBook, LibraryEventType.CREATE);
        var libraryEvent = new LibraryEvent(null, libraryEventValue);
        var expectedErrorMessage = "value.book.author.firstName - must not be blank, value.book.name - must not be blank";

        doNothing().when(libraryEventProducer).process(isA(LibraryEvent.class));

        mockMvc.perform(post("/v1/library/event")
                        .content(JsonUtilities.getJsonString(objectMapper, libraryEvent))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedErrorMessage));
    }

}
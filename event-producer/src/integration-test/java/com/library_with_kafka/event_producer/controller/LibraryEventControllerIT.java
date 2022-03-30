package com.library_with_kafka.event_producer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library_with_kafka.event_producer.EventProducerApplication;
import com.library_with_kafka.event_producer.constant.KafkaTopic;
import com.library_with_kafka.event_producer.domain.Author;
import com.library_with_kafka.event_producer.domain.Book;
import com.library_with_kafka.event_producer.domain.event.LibraryEvent;
import com.library_with_kafka.event_producer.domain.event.LibraryEventType;
import com.library_with_kafka.event_producer.domain.event.LibraryEventValue;
import com.library_with_kafka.event_producer.utilities.JsonUtilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("it")
@Slf4j
@SpringBootTest(classes = EventProducerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
class LibraryEventControllerIT {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    ObjectMapper objectMapper;

    Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp() {
        var props = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        consumer = new DefaultKafkaConsumerFactory<>(props, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    @Timeout(10)
    void saveEventSuccessful() {
        var author = new Author(null, "Name", "Surname", "test@mail.com");
        var newBook = new Book(null, "Test", BigDecimal.TEN, author);
        var libraryEventValue = new LibraryEventValue(newBook,LibraryEventType.CREATE);
        var libraryEvent = new LibraryEvent(null, libraryEventValue);
        var request = new HttpEntity<>(libraryEvent);

        var response = restTemplate.exchange("/v1/library/event", HttpMethod.POST, request, Void.class);
        log.debug("Creation event response: {};\n\tResponse code = {} ", response.toString(), response.getStatusCode());

        assertEquals(HttpStatus.OK, response.getStatusCode());

        var consumedMessage = KafkaTestUtils.getSingleRecord(consumer, KafkaTopic.LIBRARY_EVENTS.getName());
        var actualValue = JsonUtilities.getObjectFromString(objectMapper, consumedMessage.value(), LibraryEventValue.class);
        var actualEvent = new LibraryEvent(consumedMessage.key(), actualValue);

        assertEquals(libraryEvent, actualEvent);
    }

}
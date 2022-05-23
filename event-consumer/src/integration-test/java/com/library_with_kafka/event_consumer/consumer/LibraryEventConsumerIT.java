package com.library_with_kafka.event_consumer.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library_with_kafka.event_consumer.EventConsumerApplication;
import com.library_with_kafka.event_consumer.domain.event.LibraryEventType;
import com.library_with_kafka.event_consumer.repository.LibraryEventRepository;
import com.library_with_kafka.event_consumer.service.LibraryEventService;
import com.library_with_kafka.event_consumer.util.LibraryEventDataFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.library_with_kafka.event_consumer.constant.KafkaTopic.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("it")
@Slf4j
@SpringBootTest(classes = EventConsumerApplication.class)
@EmbeddedKafka(topics = {"library-events", "library-events.RETRY", "library-events.DLT"}, partitions = 3)
@TestPropertySource(properties = {
        "retryListener.library.active=false"
})
class LibraryEventConsumerIT {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    KafkaListenerEndpointRegistry listenerEndpointRegistry;

    @Autowired
    LibraryEventRepository libraryEventRepository;

    @SpyBean(reset = MockReset.BEFORE)
    LibraryEventConsumer libraryEventConsumerSpy;

    @SpyBean(reset = MockReset.BEFORE)
    LibraryEventService libraryEventServiceSpy;

    @BeforeEach
    void setUp() {

        var container = listenerEndpointRegistry.getListenerContainers()
                .stream()
                .filter(messageListenerContainer -> Objects.equals(messageListenerContainer.getGroupId(), "library-events-listener"))
                .toList()
                .get(0);

        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

//        for (MessageListenerContainer listenerContainer : listenerEndpointRegistry.getListenerContainers()) {
//            ContainerTestUtils.waitForAssignment(listenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
//        }
    }

    @SneakyThrows
    @Test
    @Order(4)
    void publishNewEvent() {
        var newEvent = LibraryEventDataFactory.getNewLibraryEvent();

        var jsonData = objectMapper.writeValueAsString(newEvent);
        log.debug("Sending value: {}", jsonData);

        kafkaTemplate.send(LIBRARY_EVENTS, 0, jsonData);

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(5, TimeUnit.SECONDS);

        verify(libraryEventConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventServiceSpy, times(1)).processKafkaRecord(isA(ConsumerRecord.class));

        var result = libraryEventRepository.findAll();
        assertEquals(1, result.size());

        var libraryEvent = result.get(0);
        assertNotNull(libraryEvent.getId());
        assertNotNull(libraryEvent.getBook().getId());
        assertNotNull(libraryEvent.getBook().getAuthor().getId());
        log.debug(libraryEvent.toString());

    }

    @SneakyThrows
    @Test
    @Order(3)
    void publishUpdateEvent() {
        var savedEvent = libraryEventRepository.save(LibraryEventDataFactory.getNewLibraryEvent());
        log.debug("Saved event: {}", savedEvent);

        final String newBookName = "Updated Book";
        savedEvent.setEventType(LibraryEventType.UPDATE);
        savedEvent.getBook().setName(newBookName);

        var jsonData = objectMapper.writeValueAsString(savedEvent);
        log.debug("Sending value: {}", jsonData);

        kafkaTemplate.send(LIBRARY_EVENTS, 0, jsonData);


        CountDownLatch latch = new CountDownLatch(1);
        latch.await(5, TimeUnit.SECONDS);

        verify(libraryEventConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventServiceSpy, times(1)).processKafkaRecord(isA(ConsumerRecord.class));

        var result = libraryEventRepository.findAll();
        assertEquals(1, result.size());

        var updatedLibraryEvent = result.get(0);
        assertEquals(newBookName, updatedLibraryEvent.getBook().getName());
        log.debug("Update event: {}", updatedLibraryEvent);
    }

    @SneakyThrows
    @Test
    @Order(2)
    void publishUpdateEventWithToRetryTopic() {
        var updateEvent = LibraryEventDataFactory.getNewLibraryEvent();
        updateEvent.setEventType(LibraryEventType.UPDATE);
        updateEvent.setId(-1L);

        var jsonData = objectMapper.writeValueAsString(updateEvent);
        log.debug("Sending value: {}", jsonData);

        kafkaTemplate.send(LIBRARY_EVENTS, 0, jsonData);

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(10, TimeUnit.SECONDS);

//        verify(libraryEventConsumerSpy, times(3)).onMessage(isA(ConsumerRecord.class));
//        verify(libraryEventServiceSpy, times(3)).processKafkaRecord(isA(ConsumerRecord.class));

        var props = KafkaTestUtils.consumerProps("test-group-1", "true", embeddedKafkaBroker);
        var consumer = new DefaultKafkaConsumerFactory<>(props, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);

        ConsumerRecord<Integer, String> consumedFromRetryTopic = KafkaTestUtils.getSingleRecord(consumer, LIBRARY_EVENTS_RETRY);

        log.debug("Consumed message from retry topic: {}", consumedFromRetryTopic);

    }

    @SneakyThrows
    @Test
    @Order(1)
    void publishUpdateEventWithToDltTopic() {
        var updateEvent = LibraryEventDataFactory.getNewLibraryEvent();
        updateEvent.setEventType(LibraryEventType.UPDATE);

        var jsonData = objectMapper.writeValueAsString(updateEvent);
        log.debug("Sending value: {}", jsonData);

        kafkaTemplate.send(LIBRARY_EVENTS, 0, jsonData);

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(10, TimeUnit.SECONDS);

//        verify(libraryEventConsumerSpy, times(3)).onMessage(isA(ConsumerRecord.class));
//        verify(libraryEventServiceSpy, times(3)).processKafkaRecord(isA(ConsumerRecord.class));

        var props = KafkaTestUtils.consumerProps("test-group-2", "true", embeddedKafkaBroker);
        var consumer = new DefaultKafkaConsumerFactory<>(props, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);

        ConsumerRecord<Integer, String> consumedFromRetryTopic = KafkaTestUtils.getSingleRecord(consumer, LIBRARY_EVENTS_DLT);

        log.debug("Consumed message from dlt topic: {}", consumedFromRetryTopic);
        consumedFromRetryTopic.headers().forEach(header -> {
            log.debug("Key: {}, Value: {}", header.key(), new String(header.value()));
        });

    }

}
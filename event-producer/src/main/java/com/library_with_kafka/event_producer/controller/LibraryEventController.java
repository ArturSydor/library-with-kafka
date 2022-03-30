package com.library_with_kafka.event_producer.controller;

import com.library_with_kafka.event_producer.domain.event.LibraryEvent;
import com.library_with_kafka.event_producer.domain.event.LibraryEventType;
import com.library_with_kafka.event_producer.producer.LibraryEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/v1/library/event")
public class LibraryEventController {

    private final LibraryEventProducer libraryEventProducer;

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody @Valid LibraryEvent libraryEvent, @RequestParam(name = "async", defaultValue = "true") boolean async) {
        libraryEvent.value().setEventType(LibraryEventType.CREATE);
        log.debug("Received new event: {}", libraryEvent);

        if (async) {
            libraryEventProducer.process(libraryEvent);
        } else {
            libraryEventProducer.processAsync(libraryEvent);
        }

        return ResponseEntity.ok().build();
    }

}

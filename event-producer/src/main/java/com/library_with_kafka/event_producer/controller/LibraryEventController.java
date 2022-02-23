package com.library_with_kafka.event_producer.controller;

import com.library_with_kafka.event_producer.domain.LibraryEvent;
import com.library_with_kafka.event_producer.domain.LibraryEventType;
import com.library_with_kafka.event_producer.producer.LibraryEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/v1/library/event")
public class LibraryEventController {

    private final LibraryEventProducer libraryEventProducer;

    @PostMapping
    public ResponseEntity<?> save(@RequestBody LibraryEvent libraryEvent, @RequestParam(name = "async", defaultValue = "true") boolean async) {
        libraryEvent.setEventType(LibraryEventType.CREATE);
        log.debug("Received new event: {}", libraryEvent);
        libraryEventProducer.process(libraryEvent, async);
        return ResponseEntity.ok().build();
    }

}

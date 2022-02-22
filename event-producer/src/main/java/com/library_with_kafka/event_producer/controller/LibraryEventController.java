package com.library_with_kafka.event_producer.controller;

import com.library_with_kafka.event_producer.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/library/event")
public class LibraryEventController {

    @PostMapping
    public ResponseEntity<?> save(@RequestBody LibraryEvent libraryEvent) {
        log.debug("Received new event: {}", libraryEvent);
        // TODO calling kafka
        return ResponseEntity.ok().build();
    }

}

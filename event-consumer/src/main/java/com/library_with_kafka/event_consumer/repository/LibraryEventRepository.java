package com.library_with_kafka.event_consumer.repository;

import com.library_with_kafka.event_consumer.domain.event.LibraryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryEventRepository extends JpaRepository<LibraryEvent, Long> {
}

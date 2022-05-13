package com.library_with_kafka.event_consumer.domain;

import com.library_with_kafka.event_consumer.domain.event.LibraryEvent;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
public class Book {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "author_id")
    private Author author;

    @OneToOne
    @JoinColumn(name = "library_event_id")
    @ToString.Exclude
    private LibraryEvent libraryEvent;

}

package com.library_with_kafka.event_producer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

import static com.library_with_kafka.event_producer.constant.KafkaTopic.LIBRARY_EVENTS;

@Configuration
@Profile("local")
public class KafkaTopicAutoCreationConfig {

    @Bean
    public NewTopic libraryTopic() {
        return TopicBuilder
                .name(LIBRARY_EVENTS.getName())
                .partitions(LIBRARY_EVENTS.getPartitions())
                .replicas(LIBRARY_EVENTS.getReplication())
                .build();
    }

}

package com.library_with_kafka.event_consumer.config;

import com.library_with_kafka.event_consumer.exception.RecoverableException;
import com.library_with_kafka.event_consumer.util.TopicUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

import java.util.List;

import static com.library_with_kafka.event_consumer.constant.KafkaTopic.*;

@Slf4j
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public DeadLetterPublishingRecoverer publishRecoverer(KafkaTemplate<Integer, String> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
                (r, e) -> {
                    if (e.getCause() instanceof RecoverableException) {
                        return new TopicPartition(LIBRARY_EVENTS_RETRY, r.partition());
                    } else {
                        return new TopicPartition(LIBRARY_EVENTS_DLT, r.partition());
                    }
                });
    }

    @Bean
    CommonErrorHandler customErrorHandler(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {

        var exceptionToIgnoreList = List.of(IllegalArgumentException.class);

        var exceptionToRetryList = List.of(RecoverableException.class);

        var fixedBackOff = new FixedBackOff(1000L, 2);

        var expBackOff = new ExponentialBackOffWithMaxRetries(2);
        expBackOff.setInitialInterval(1_000L);
        expBackOff.setMultiplier(2);
        expBackOff.setMaxInterval(10_000L);

        var defaultErrorHandler = new DefaultErrorHandler(
                deadLetterPublishingRecoverer,
//                fixedBackOff
                expBackOff
        );

        defaultErrorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.info("Failed Record in Retry Listener. \nException: {} \nDeliveryAttempt: {}", ex.getMessage(), deliveryAttempt);
        });

        exceptionToIgnoreList.forEach(defaultErrorHandler::addNotRetryableExceptions);

        exceptionToRetryList.forEach(defaultErrorHandler::addRetryableExceptions);

        return defaultErrorHandler;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory,
            CommonErrorHandler commonErrorHandler) {
        var factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        // factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(commonErrorHandler);
        return factory;
    }


}

package com.library_with_kafka.event_consumer.util;

import lombok.experimental.UtilityClass;

import java.util.Objects;

public final class TopicUtil {

    private static final String RETRY_SUFFIX = ".RETRY";

    private static final String DLT_SUFFIX = ".DLT";

    public static String getRetryTopic(String originalTopic) {
        Objects.requireNonNull(originalTopic);
        return originalTopic + RETRY_SUFFIX;
    }

    public static String getDltTopic(String originalTopic) {
        Objects.requireNonNull(originalTopic);
        return originalTopic + DLT_SUFFIX;
    }

}

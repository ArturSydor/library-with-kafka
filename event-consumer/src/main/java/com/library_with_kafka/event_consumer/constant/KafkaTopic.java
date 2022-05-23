package com.library_with_kafka.event_consumer.constant;

import com.library_with_kafka.event_consumer.util.TopicUtil;

public final class KafkaTopic {

    private static final String RETRY_SUFFIX = ".RETRY";
    private static final String DLT_SUFFIX = ".DLT";

    public static final String LIBRARY_EVENTS = "library-events";
    public static final String LIBRARY_EVENTS_RETRY = LIBRARY_EVENTS + RETRY_SUFFIX;
    public static final String LIBRARY_EVENTS_DLT = LIBRARY_EVENTS + DLT_SUFFIX;


}

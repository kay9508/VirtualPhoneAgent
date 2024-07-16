package com.sinyoung.prod;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface TopicEventSource {
    String TOPIC_OUTPUT_CHANNEL4 = "monitorChannel";
    @Output(TOPIC_OUTPUT_CHANNEL4)
    MessageChannel monitorChannel();
}

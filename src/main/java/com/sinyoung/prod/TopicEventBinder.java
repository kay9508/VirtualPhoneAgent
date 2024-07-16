package com.sinyoung.prod;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableBinding(TopicEventSource.class)
public class TopicEventBinder implements EventProducer {

    private final TopicEventSource topicEventSource;

    public TopicEventBinder(TopicEventSource topicEventSource) {
        this.topicEventSource = topicEventSource;
    }

    @Override
    public void publish(String payload) {
        boolean result = false;
//        QueueType queueType = QueueType.getItem(queue);

        log.info("payload : [{}]", payload);
        result = topicEventSource.monitorChannel().send(MessageBuilder.withPayload(payload).build());
        // before 정보
        /*switch (channel) {
            case "alarmChannel" :
                log.info("alarmChannel");
                Message<String> message = MessageBuilder.withPayload(payload).build();
                result = topicEventSource.alarmChannel().send(message);
                break;
            case "eventChannel" :
                log.info("eventChannel");
                result = topicEventSource.eventChannel().send(MessageBuilder.withPayload(payload).build());
                break;
            case "noticeChannel" :
                log.info("noticeChannel");
                result = topicEventSource.noticeChannel().send(MessageBuilder.withPayload(payload).build());
                break;
            case "monitorChannel" :
                log.info("monitorChannel");
                result = topicEventSource.monitorChannel().send(MessageBuilder.withPayload(payload).build());
                break;
            case "customChannel" :
                log.info("customChannel");
                result = topicEventSource.customChannel().send(MessageBuilder.withPayload(payload).build());
                break;
            default:
                break;
        }*/
    }

}
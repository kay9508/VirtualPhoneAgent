package com.sinyoung.service;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.sinyoung.prod.TopicEventBinder;

@Service
@RequiredArgsConstructor
public class RabbitmqProducer {

    private final RabbitTemplate rabbitTemplate;
    private final TopicEventBinder topicEventBinder;

    @Value("${spring.rabbitmq.template.exchange}")
    private String EXCHANGE;
    @Value("${spring.rabbitmq.template.log.routing-key}")
    private String LOGROUTINGKEY;
    @Value("${spring.rabbitmq.template.comm-log.routing-key}")
    private String COMMROUTINGKEY;

    public void sendLog(String message) {
        //TODO 메시지 형식 정하고 DTO만들어서 json으로 변환해서 보내기
        rabbitTemplate.convertAndSend(EXCHANGE, LOGROUTINGKEY, message);
    }

    public void sendCommLog(String message) {
        //TODO 메시지 형식 정하고 DTO만들어서 json으로 변환해서 보내기
        rabbitTemplate.convertAndSend(EXCHANGE, COMMROUTINGKEY, message);
    }

    public void sendLogLevel2(String message) {
        rabbitTemplate.convertAndSend(EXCHANGE, LOGROUTINGKEY, message);
        topicEventBinder.publish(message);
    }
}

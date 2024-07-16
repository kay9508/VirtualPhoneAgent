package com.sinyoung;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class VirtualPhoneAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualPhoneAgentApplication.class, args);
		//TODO 최초 실행시 Management로 Agent정보 보낼지 결정
	}

}

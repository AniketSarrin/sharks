package com.sharks.event.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.sharks.event.config.EventRabbitProperties;

@Service
public class EventCreatedPublisher {

	private static final Logger log = LoggerFactory.getLogger(EventCreatedPublisher.class);

	private final RabbitTemplate rabbitTemplate;
	private final EventRabbitProperties rabbitProperties;

	public EventCreatedPublisher(RabbitTemplate rabbitTemplate, EventRabbitProperties rabbitProperties) {
		this.rabbitTemplate = rabbitTemplate;
		this.rabbitProperties = rabbitProperties;
	}

	public void publish(EventCreatedMessage message) {
		log.info("Publishing event created: eventId={}", message.eventId());
		rabbitTemplate.convertAndSend(
				rabbitProperties.getEventExchange(),
				rabbitProperties.getEventCreatedRoutingKey(),
				message
		);
	}
}

package com.sharks.user.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.sharks.user.config.UserRabbitProperties;

/**
 * Publishes user lifecycle events to {@code sharks.user} for auth and other consumers.
 */
@Service
public class UserEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);

	private final RabbitTemplate rabbitTemplate;
	private final UserRabbitProperties rabbitProperties;

	public UserEventPublisher(RabbitTemplate rabbitTemplate, UserRabbitProperties rabbitProperties) {
		this.rabbitTemplate = rabbitTemplate;
		this.rabbitProperties = rabbitProperties;
	}

	public void publishUserCreated(UUID id, String email, String password, String role) {
		log.info("Publishing user.created: id={}, email={}, role={}", id, email, role);
		UserCreatedMessage message = new UserCreatedMessage(id, email, password, role);
		rabbitTemplate.convertAndSend(
				rabbitProperties.getUserExchange(),
				rabbitProperties.getUserRoutingKey(),
				message);
	}

	/**
	 * Notifies auth to delete the Supabase user by email (matches auth {@code UserDeletedMessage}).
	 */
	public void publishUserDeletedForAuth(String email) {
		log.info("Publishing user.deleted (auth payload): email={}", email);
		rabbitTemplate.convertAndSend(
				rabbitProperties.getUserExchange(),
				"user.deleted",
				new UserDeletedAuthPayload(email));
	}
}

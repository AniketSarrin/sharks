package com.sharks.user.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Listens for user.created events from auth service.
 * When a new user is created in auth, this service can perform user service-specific operations.
 */
@Service
public class UserCreatedListener {

	private static final Logger log = LoggerFactory.getLogger(UserCreatedListener.class);

	public UserCreatedListener() {
	}

	@RabbitListener(queues = "${user.rabbitmq.user-queue}")
	public void onUserCreated(UserCreatedMessage message) {
		log.info("User created event received: id={} email={}", message.id(), message.email());
		// TODO: Implement user profile creation or other user service operations
		// e.g., Create user profile in user service database
	}
}

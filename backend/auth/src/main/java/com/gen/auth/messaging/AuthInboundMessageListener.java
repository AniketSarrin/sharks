package com.gen.auth.messaging;

import com.gen.auth.model.AppRole;
import com.gen.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AuthInboundMessageListener {

	private static final Logger log = LoggerFactory.getLogger(AuthInboundMessageListener.class);

	private final AuthService authService;

	public AuthInboundMessageListener(AuthService authService) {
		this.authService = authService;
	}

	@RabbitListener(queues = "auth.user.created")
	public void onUserCreated(UserCreatedMessage message) {
		if (message.id() == null) {
			throw new IllegalArgumentException("id is required for auth.user.created messages");
		}
		AppRole role = parseRole(message.role());
		log.info(
			"auth.user.created received id={} email={} role={}",
			message.id(),
			message.email(),
			role
		);
		authService.createUser(message.id(), message.email(), message.password(), role);
	}

	@RabbitListener(queues = "auth.user.deleted")
	public void onUserDeleted(UserDeletedMessage message) {
		log.info(
			"auth.user.deleted received email={}",
			message.email()
		);
		authService.deleteUserByEmail(message.email());
	}

	private AppRole parseRole(String rawRole) {
		if (rawRole == null) {
			throw new IllegalArgumentException("Role is required for user.created messages");
		}

		try {
			return AppRole.valueOf(rawRole.toUpperCase());
		} catch (IllegalArgumentException ignored) {
			return AppRole.fromDbValue(rawRole.toLowerCase());
		}
	}
}

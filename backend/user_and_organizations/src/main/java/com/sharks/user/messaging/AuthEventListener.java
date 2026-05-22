package com.sharks.user.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Listener for auth-related events published by the auth service.
 * Handles user.provisioned and role.changed messages from the auth service.
 */
@Service
public class AuthEventListener {

	private static final Logger log = LoggerFactory.getLogger(AuthEventListener.class);

	/**
	 * Handles user.provisioned events from the auth service.
	 * This is called when a user is successfully provisioned in the auth service.
	 */
	@RabbitListener(queues = "${user.rabbitmq.user-provisioned-queue:user.user.provisioned}")
	public void onUserProvisioned(UserProvisionedMessage message) {
		log.info("Received user.provisioned event: userId={}, email={}", message.userId(), message.email());
		// TODO: Implement user provisioning logic in user service
		// This could include:
		// - Updating user status to provisioned
		// - Notifying other services
		// - Initializing user-specific resources
	}

	/**
	 * Handles role.changed events from the auth service.
	 * This is called when a user's role is changed in the auth service.
	 */
	@RabbitListener(queues = "${user.rabbitmq.role-changed-queue:user.role.changed}")
	public void onRoleChanged(RoleChangedMessage message) {
		log.info("Received role.changed event: userId={}, newRole={}", message.userId(), message.newRole());
		// TODO: Implement role change handling
		// This could include:
		// - Updating user role in user database
		// - Updating user permissions
		// - Notifying frontend about role change
	}
}

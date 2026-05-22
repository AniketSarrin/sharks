package com.gen.auth.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.gen.auth.model.AppRole;
import com.gen.auth.service.AuthService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthInboundMessageListenerTest {

	private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

	@Mock
	private AuthService authService;

	@InjectMocks
	private AuthInboundMessageListener listener;

	@Test
	void onUserCreated_delegatesToAuthService() {
		UserCreatedMessage message = new UserCreatedMessage(USER_ID, "person@example.com", "secret", "attendee");

		listener.onUserCreated(message);

		verify(authService).createUser(eq(USER_ID), eq("person@example.com"), eq("secret"), eq(AppRole.ATTENDEE));
	}

	@Test
	void onUserCreated_missingId_throws() {
		UserCreatedMessage message = new UserCreatedMessage(null, "person@example.com", "secret", "attendee");

		assertThatThrownBy(() -> listener.onUserCreated(message))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("id is required");
	}

	@Test
	void onUserDeleted_delegatesToDeleteByEmail() {
		UserDeletedMessage message = new UserDeletedMessage("person@example.com");

		listener.onUserDeleted(message);

		verify(authService).deleteUserByEmail("person@example.com");
	}
}

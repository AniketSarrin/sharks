package com.gen.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gen.auth.client.GotrueAdminCreateUserRequest;
import com.gen.auth.client.GotrueAdminUserResponse;
import com.gen.auth.client.SupabaseAuthClient;
import com.gen.auth.exception.SupabaseAuthException;
import com.gen.auth.model.AppRole;
import com.gen.auth.model.UserRole;
import com.gen.auth.repo.UserRoleRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

	@Mock
	private SupabaseAuthClient supabaseAuthClient;

	@Mock
	private UserRoleRepository userRoleRepository;

	@InjectMocks
	private AuthService authService;

	@Test
	void createUser_withPredefinedId_sendsIdToSupabaseAndSavesRole() {
		GotrueAdminUserResponse created = new GotrueAdminUserResponse(USER_ID.toString(), "pre@example.com");
		when(supabaseAuthClient.adminCreateUser(any())).thenReturn(created);

		GotrueAdminUserResponse result = authService.createUser(USER_ID, "pre@example.com", "pw", AppRole.ATTENDEE);

		assertThat(result).isSameAs(created);
		ArgumentCaptor<GotrueAdminCreateUserRequest> captor = ArgumentCaptor.forClass(GotrueAdminCreateUserRequest.class);
		verify(supabaseAuthClient).adminCreateUser(captor.capture());
		assertThat(captor.getValue().id()).isEqualTo(USER_ID);
		verify(userRoleRepository).save(argThat(ur -> USER_ID.equals(ur.getUserId()) && ur.getRole() == AppRole.ATTENDEE));
	}

	@Test
	void deleteUser_supabaseSuccess_deletesRoles() {
		List<UserRole> roles = List.of(new UserRole(USER_ID, AppRole.ATTENDEE));
		when(userRoleRepository.findByUserId(USER_ID)).thenReturn(roles);

		authService.deleteUser(USER_ID);

		verify(supabaseAuthClient).adminDeleteUser(USER_ID);
		verify(userRoleRepository).deleteAll(roles);
	}

	@Test
	void deleteUser_supabase404_stillDeletesRoles() {
		doThrow(new SupabaseAuthException(404, "{}")).when(supabaseAuthClient).adminDeleteUser(USER_ID);
		when(userRoleRepository.findByUserId(USER_ID)).thenReturn(List.of());

		authService.deleteUser(USER_ID);

		verify(userRoleRepository).deleteAll(List.of());
	}

	@Test
	void deleteUser_supabaseOtherError_doesNotDeleteRoles() {
		doThrow(new SupabaseAuthException(400, "{}")).when(supabaseAuthClient).adminDeleteUser(USER_ID);

		assertThatThrownBy(() -> authService.deleteUser(USER_ID))
			.isInstanceOf(SupabaseAuthException.class)
			.extracting("statusCode")
			.isEqualTo(400);

		verify(userRoleRepository, never()).deleteAll(any());
	}

	@Test
	void deleteUserByEmail_userFound_deletesResolvedUser() {
		GotrueAdminUserResponse adminUser = new GotrueAdminUserResponse(USER_ID.toString(), "test@example.com");
		List<UserRole> roles = List.of(new UserRole(USER_ID, AppRole.ATTENDEE));
		when(supabaseAuthClient.findUserByEmail("test@example.com")).thenReturn(Optional.of(adminUser));
		when(userRoleRepository.findByUserId(USER_ID)).thenReturn(roles);

		authService.deleteUserByEmail("test@example.com");

		verify(supabaseAuthClient).adminDeleteUser(USER_ID);
		verify(userRoleRepository).deleteAll(roles);
	}

	@Test
	void deleteUserByEmail_userNotFound_noDeleteIsAttempted() {
		when(supabaseAuthClient.findUserByEmail("missing@example.com")).thenReturn(Optional.empty());

		authService.deleteUserByEmail("missing@example.com");

		verify(supabaseAuthClient, never()).adminDeleteUser(any());
		verify(userRoleRepository, never()).deleteAll(any());
	}
}

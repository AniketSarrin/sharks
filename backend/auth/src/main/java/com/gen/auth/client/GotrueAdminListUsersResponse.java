package com.gen.auth.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GotrueAdminListUsersResponse(List<GotrueAdminUserResponse> users) {
}

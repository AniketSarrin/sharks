package com.sharks.user.controller;

import com.sharks.user.dto.RegisterRequest;
import com.sharks.user.dto.UserDto;
import com.sharks.user.security.AuthPrincipal;
import com.sharks.user.service.AuthService;
import com.sharks.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/me")
    public UserDto getMyProfile(@AuthenticationPrincipal AuthPrincipal principal) {
        log.info("[jwt:controller] layer=UserController, endpoint=GET /me, step=invoke, principal={}", principal);
        return userService.getCurrentUser(principal.userId().toString());
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyProfile(@AuthenticationPrincipal AuthPrincipal principal) {
        log.info("[jwt:controller] layer=UserController, endpoint=DELETE /me, step=invoke, principal={}", principal);
        userService.deleteCurrentUser(principal.userId().toString());
    }
}

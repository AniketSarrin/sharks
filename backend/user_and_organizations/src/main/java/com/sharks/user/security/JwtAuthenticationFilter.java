package com.sharks.user.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Bearer JWT authentication (same pattern as events and ticketing services).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        log.debug("[jwt:filter] layer=JwtAuthenticationFilter, step=enter, method={}, path={}", method, path);

        try {
            boolean hasAuthHeader = StringUtils.hasText(request.getHeader("Authorization"));
            log.debug("[jwt:filter] layer=JwtAuthenticationFilter, step=authorization-header, present={}",
                    hasAuthHeader);

            String token = parseBearerToken(request);
            if (token == null) {
                log.debug("[jwt:filter] layer=JwtAuthenticationFilter, step=no-bearer-token, continuing chain");
                filterChain.doFilter(request, response);
                return;
            }

            log.info("[jwt:filter] layer=JwtAuthenticationFilter, step=bearer-present, tokenLength={}", token.length());

            if (!jwtUtils.isConfigured()) {
                log.warn("[jwt:filter] layer=JwtAuthenticationFilter, step=abort, reason=JwtUtils not configured");
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("[jwt:filter] layer=JwtAuthenticationFilter, step=calling parseAndVerify");

            var claims = jwtUtils.parseAndVerify(token);
            log.debug("[jwt:filter] layer=JwtAuthenticationFilter, step=claims-received, calling getUserId");

            var userId = jwtUtils.getUserId(claims);
            log.debug("[jwt:filter] layer=JwtAuthenticationFilter, step=userId-resolved, calling getRole");

            var role = jwtUtils.getRole(claims);
            if (role == null) {
                log.warn("[jwt:filter] layer=JwtAuthenticationFilter, step=fail, reason=role null after getRole");
                throw new BadJOSEException("JWT missing user_role or app_metadata.role");
            }

            String email = null;
            try {
                String e = claims.getStringClaim("email");
                if (StringUtils.hasText(e)) {
                    email = e;
                }
            } catch (ParseException pe) {
                log.debug("[jwt:filter] layer=JwtAuthenticationFilter, step=email-claim, result=skip, message={}",
                        pe.getMessage());
            }

            var principal = new AuthPrincipal(userId, role, email);
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name().toUpperCase()));
            var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("[jwt:filter] layer=JwtAuthenticationFilter, step=security-context-set, userId={}, role={}, "
                            + "authorities={}, emailPresent={}",
                    userId, role, authorities, email != null);
        } catch (BadJOSEException | ParseException | JOSEException | IllegalArgumentException | IllegalStateException e) {
            log.warn("[jwt:filter] layer=JwtAuthenticationFilter, step=exception, type={}, message={}",
                    e.getClass().getSimpleName(), e.getMessage());
            log.debug("[jwt:filter] layer=JwtAuthenticationFilter, step=exception-detail", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
        log.trace("[jwt:filter] layer=JwtAuthenticationFilter, step=exit, path={}", path);
    }

    private static String parseBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }
}

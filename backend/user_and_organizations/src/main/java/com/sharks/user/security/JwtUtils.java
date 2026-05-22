package com.sharks.user.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * OIDC JWT verification: loads {@code issuer} and {@code jwks_uri} from the discovery document,
 * then verifies RS* / ES* signatures using JWKS. The JWS {@code kid} selects the matching public key
 * (via {@link JWSVerificationKeySelector} + {@link RemoteJWKSet}). HMAC tokens are not supported.
 */
@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    private final String expectedIssuer;
    private final String jwksUri;
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    public JwtUtils(@Value("${security.oidc.discovery-url:}") String discoveryUrl) throws IOException {
        log.info("[jwt:config] JwtUtils init — layer=constructor, step=start, mode=OIDC_JWKS_RS_ES");
        if (!StringUtils.hasText(discoveryUrl)) {
            this.expectedIssuer = null;
            this.jwksUri = null;
            this.jwtProcessor = null;
            log.warn("[jwt:config] JwtUtils init — layer=constructor, step=abort, reason=security.oidc.discovery-url is blank "
                    + "(set SUPABASE_OIDC_DISCOVERY_URL in repo root .env)");
            return;
        }

        log.info("[jwt:config] JwtUtils init — layer=discovery-fetch, url={}", discoveryUrl);
        DiscoveryDocument discoveryDocument = readDiscoveryDocument(discoveryUrl, new ObjectMapper());
        String issuer = discoveryDocument.issuer();
        this.expectedIssuer = issuer;
        this.jwksUri = discoveryDocument.jwksUri();
        URL jwksUrl = new URL(jwksUri);
        log.info("[jwt:config] JwtUtils init — layer=discovery-parse, issuer={}, jwks_uri={}", issuer, jwksUrl);

        JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(jwksUrl);
        Set<JWSAlgorithm> acceptedAlgorithms = new HashSet<>();
        acceptedAlgorithms.addAll(JWSAlgorithm.Family.RSA);
        acceptedAlgorithms.addAll(JWSAlgorithm.Family.EC);
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(acceptedAlgorithms, keySource);

        DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(keySelector);
        final String iss = issuer;
        processor.setJWTClaimsSetVerifier((claims, context) -> verifyIssuerAndExpiry(claims, iss));

        this.jwtProcessor = processor;
        log.info("[jwt:config] JwtUtils init — layer=constructor, step=complete, "
                + "algorithms=RSA+EC, jwksKidResolution=JWSVerificationKeySelector+RemoteJWKSet");
    }

    private static void verifyIssuerAndExpiry(JWTClaimsSet claims, String issuer) throws BadJWTException {
        String tokenIss = claims.getIssuer();
        log.debug("[jwt:verify-claims] layer=jwt-processor, step=issuer-check, tokenIss={}, expectedIss={}",
                tokenIss, issuer);
        if (!StringUtils.hasText(tokenIss) || !tokenIss.equals(issuer)) {
            log.warn("[jwt:verify-claims] layer=jwt-processor, step=issuer-check, result=fail");
            throw new BadJWTException("Invalid token issuer");
        }
        Date expiresAt = claims.getExpirationTime();
        Instant now = Instant.now();
        log.debug("[jwt:verify-claims] layer=jwt-processor, step=exp-check, exp={}, now={}", expiresAt, now);
        if (expiresAt == null || expiresAt.toInstant().isBefore(now)) {
            log.warn("[jwt:verify-claims] layer=jwt-processor, step=exp-check, result=fail");
            throw new BadJWTException("Token is expired");
        }
        log.debug("[jwt:verify-claims] layer=jwt-processor, step=issuer-and-exp, result=ok");
    }

    private static DiscoveryDocument readDiscoveryDocument(String discoveryUrl, ObjectMapper objectMapper)
            throws IOException {
        log.debug("[jwt:discovery] layer=http, step=connect, url={}", discoveryUrl);
        HttpURLConnection connection = (HttpURLConnection) new URL(discoveryUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);

        int status = connection.getResponseCode();
        log.debug("[jwt:discovery] layer=http, step=response, status={}", status);
        if (status < 200 || status >= 300) {
            throw new IllegalStateException("Failed to fetch OIDC discovery document: HTTP " + status);
        }

        try (var inputStream = connection.getInputStream()) {
            JsonNode root = objectMapper.readTree(inputStream);
            String issuer = root.path("issuer").asText();
            String jwksUri = root.path("jwks_uri").asText();
            if (!StringUtils.hasText(issuer) || !StringUtils.hasText(jwksUri)) {
                throw new IllegalStateException("OIDC discovery document missing issuer or jwks_uri");
            }
            log.debug("[jwt:discovery] layer=parse, step=complete, issuerPresent={}, jwksUriPresent={}",
                    StringUtils.hasText(issuer), StringUtils.hasText(jwksUri));
            return new DiscoveryDocument(issuer, jwksUri);
        }
    }

    private record DiscoveryDocument(String issuer, String jwksUri) {
    }

    public boolean isConfigured() {
        return jwtProcessor != null && StringUtils.hasText(expectedIssuer);
    }

    public JWTClaimsSet parseAndVerify(String token)
            throws ParseException, JOSEException, BadJWTException, BadJOSEException {
        log.info("[jwt:parse] layer=JwtUtils.parseAndVerify, step=start, tokenLength={}",
                token != null ? token.length() : 0);
        if (!isConfigured()) {
            log.error("[jwt:parse] layer=JwtUtils.parseAndVerify, step=abort, reason=JWT verifier not configured");
            throw new IllegalStateException("security.oidc.discovery-url is not configured");
        }
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSAlgorithm alg = signedJWT.getHeader().getAlgorithm();
        String algName = alg != null ? alg.getName() : "unknown";
        String kid = signedJWT.getHeader().getKeyID();
        log.info("[jwt:parse] layer=JwtUtils.parseAndVerify, step=jws-header, alg={}, kid={} (kid selects JWK from {})",
                algName, kid, jwksUri);

        if (alg != null && JWSAlgorithm.Family.HMAC_SHA.contains(alg)) {
            log.error("[jwt:parse] layer=JwtUtils.parseAndVerify, step=reject, alg={}, reason=HMAC not supported; use RS*/ES* + JWKS",
                    algName);
            throw new BadJOSEException(
                    "JWT algorithm " + algName
                            + " is symmetric (HMAC). This service only verifies asymmetric RS*/ES* tokens using OIDC JWKS.");
        }

        try {
            JWTClaimsSet claims = jwtProcessor.process(signedJWT, null);
            log.info("[jwt:parse] layer=JwtUtils.parseAndVerify, step=jws-verify-ok, sub={}, iss={}, exp={}",
                    claims.getSubject(), claims.getIssuer(), claims.getExpirationTime());
            return claims;
        } catch (BadJOSEException | JOSEException e) {
            log.warn("[jwt:parse] layer=JwtUtils.parseAndVerify, step=jws-verify-fail, type={}, message={}",
                    e.getClass().getSimpleName(), e.getMessage());
            log.debug("[jwt:parse] layer=JwtUtils.parseAndVerify, step=jws-verify-fail-detail", e);
            throw e;
        }
    }

    public UUID getUserId(JWTClaimsSet claims) {
        log.debug("[jwt:claims] layer=getUserId, step=start");
        String sub = claims.getSubject();
        if (!StringUtils.hasText(sub)) {
            log.warn("[jwt:claims] layer=getUserId, step=fail, reason=missing sub");
            throw new IllegalArgumentException("JWT missing sub claim");
        }
        UUID id = UUID.fromString(sub);
        log.debug("[jwt:claims] layer=getUserId, step=ok, userId={}", id);
        return id;
    }

    public AppRole getRole(JWTClaimsSet claims) {
        log.debug("[jwt:claims] layer=getRole, step=resolve");
        String fromUserRole;
        try {
            fromUserRole = claims.getStringClaim("user_role");
        } catch (ParseException e) {
            log.warn("[jwt:claims] layer=getRole, step=user_role, result=parse-error, message={}", e.getMessage());
            throw new IllegalArgumentException("JWT user_role claim is not a string", e);
        }
        if (StringUtils.hasText(fromUserRole)) {
            AppRole role = AppRole.fromDbValue(fromUserRole);
            log.info("[jwt:claims] layer=getRole, step=ok, source=user_role, role={}", role);
            return role;
        }
        Object appMeta = claims.getClaim("app_metadata");
        if (appMeta instanceof Map<?, ?> map) {
            Object role = map.get("role");
            if (role instanceof String s && StringUtils.hasText(s)) {
                AppRole appRole = AppRole.fromDbValue(s);
                log.info("[jwt:claims] layer=getRole, step=ok, source=app_metadata.role, role={}", appRole);
                return appRole;
            }
            log.debug("[jwt:claims] layer=getRole, step=app_metadata, result=no-string-role, keys={}", map.keySet());
        } else {
            log.debug("[jwt:claims] layer=getRole, step=app_metadata, result=absent-or-not-map");
        }
        log.warn("[jwt:claims] layer=getRole, step=fail, reason=no user_role and no app_metadata.role");
        return null;
    }
}

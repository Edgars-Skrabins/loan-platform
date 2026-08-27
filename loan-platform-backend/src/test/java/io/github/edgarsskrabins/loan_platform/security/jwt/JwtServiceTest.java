package io.github.edgarsskrabins.loan_platform.security.jwt;

import io.github.edgarsskrabins.loan_platform.user.entity.Role;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET = "test-secret-key-for-testing-purposes-at-least-32-chars-1234567890";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET);
    }

    @Test
    @DisplayName("a generated token round-trips back to the user's email")
    void generatedTokenRoundTrips() {
        String token = jwtService.generateToken(user("ada@example.com"));

        assertThat(jwtService.extractUsername(token)).isEqualTo("ada@example.com");
    }

    @Test
    @DisplayName("the token carries no role claim, only the email subject")
    void tokenCarriesOnlySubject() {
        Claims claims = parse(jwtService.generateToken(user("ada@example.com")));

        assertThat(claims.getSubject()).isEqualTo("ada@example.com");
        assertThat(claims).doesNotContainKey("role");
    }

    @Test
    @DisplayName("the token expires 24 hours after it is issued")
    void tokenExpiresIn24Hours() {
        Claims claims = parse(jwtService.generateToken(user("ada@example.com")));

        Duration lifetime = Duration.between(
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant()
        );
        assertThat(lifetime).isCloseTo(Duration.ofHours(24), Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("a token whose payload was tampered with is rejected")
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken(user("ada@example.com"));
        String[] parts = token.split("\\.");
        String forged = parts[0] + "." + parts[1].substring(0, parts[1].length() - 2) + "AA." + parts[2];

        assertThatThrownBy(() -> jwtService.extractUsername(forged))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("a token signed with an unrelated key is rejected")
    void tokenSignedWithForeignKeyIsRejected() {
        String foreign = Jwts.builder()
                .subject("attacker@example.com")
                .signWith(Jwts.SIG.HS256.key().build())
                .compact();

        assertThatThrownBy(() -> jwtService.extractUsername(foreign))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("a token with no signature at all is rejected")
    void unsignedTokenIsRejected() {
        String unsigned = Jwts.builder().subject("attacker@example.com").compact();

        assertThatThrownBy(() -> jwtService.extractUsername(unsigned))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("garbage input is rejected rather than parsed")
    void garbageIsRejected() {
        assertThatThrownBy(() -> jwtService.extractUsername("not-a-jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("an expired token is rejected")
    void expiredTokenIsRejected() {
        SecretKey key = (SecretKey) ReflectionTestUtils.getField(jwtService, "key");
        String expired = Jwts.builder()
                .subject("ada@example.com")
                .expiration(java.util.Date.from(java.time.Instant.now().minus(1, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> jwtService.extractUsername(expired))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    @DisplayName("the signing key is stable across instances with same secret")
    void signingKeyIsStableAcrossInstances() {
        String token = new JwtService(TEST_SECRET).generateToken(user("ada@example.com"));

        assertThat(new JwtService(TEST_SECRET).extractUsername(token)).isEqualTo("ada@example.com");
    }

    private Claims parse(String token) {
        SecretKey key = (SecretKey) ReflectionTestUtils.getField(jwtService, "key");
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private static User user(String email) {
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setRole(Role.CUSTOMER);
        return user;
    }
}

package io.github.edgarsskrabins.loan_platform.security.jwt;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

//    private final SecretKey key;
//
//    public JwtService(@Value("${jwt.secret}") String secret) {
//        this.key = Keys.hmacShaKeyFor(
//                Decoders.BASE64.decode(secret)
//        );
//    }

    private final SecretKey key = Jwts.SIG.HS256.key().build();

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + 86400000)
                )
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}

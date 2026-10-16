package com.example.bookcatalog.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtCryptoConfig {
    @Bean
    RSAPrivateKey jwtPrivateKey(
            @Value("${app.jwt.private-key}") Resource pem)
            throws IOException, GeneralSecurityException {
        byte[] der = readPem(pem, "PRIVATE KEY");
        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    @Bean
    RSAPublicKey jwtPublicKey(
            @Value("${app.jwt.public-key}") Resource pem)
            throws IOException, GeneralSecurityException {
        byte[] der = readPem(pem, "PUBLIC KEY");
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(der));
    }

    private static byte[] readPem(Resource resource, String label)
            throws IOException {
        String content;
        try (var input = resource.getInputStream()) {
            content = new String(input.readAllBytes(), StandardCharsets.US_ASCII);
        }
        String begin = "-----BEGIN " + label + "-----";
        String end = "-----END " + label + "-----";
        if (!content.contains(begin) || !content.contains(end)) {
            throw new IllegalArgumentException("Expected " + label + " PEM format");
        }
        String body = content.replace(begin, "").replace(end, "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(body);
    }

    @Bean
    JwtEncoder jwtEncoder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        return NimbusJwtEncoder.withKeyPair(publicKey, privateKey).build();
    }

    @Bean
    JwtDecoder jwtDecoder(
            RSAPublicKey publicKey,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.audience}") String audience) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey)
                .build(); // RS256 expected by default for this builder

        OAuth2TokenValidator<Jwt> validator =
                new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefaultWithIssuer(issuer),
                        new JwtClaimValidator<List<String>>(
                                JwtClaimNames.AUD,
                                audiences -> audiences != null
                                        && audiences.contains(audience)),
                        new JwtClaimValidator<Instant>(
                                JwtClaimNames.EXP, Objects::nonNull),
                        new JwtClaimValidator<String>(
                                JwtClaimNames.SUB,
                                subject -> subject != null && !subject.isBlank()));
        decoder.setJwtValidator(validator);
        return decoder;
    }
}

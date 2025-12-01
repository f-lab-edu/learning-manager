package me.chan99k.learningmanager.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import me.chan99k.learningmanager.domain.member.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static KeyPair generateRsaKey() {
		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests(req -> req
				.requestMatchers(
					"/api/v1/auth/**",

					// 회원가입 관련
					"/api/v1/members/register",
					"/api/v1/members/activate",

					// 비밀번호 재설정
					"/api/v1/members/reset-password",
					"/api/v1/members/confirm-reset-password",

					// 공개 정보
					"/api/v1/members/*/profile-public"
				).permitAll()
				.anyRequest().authenticated()
			)
			.oauth2ResourceServer(oauth2 -> oauth2
				.jwt(jwt -> jwt.decoder(jwtDecoder()))
			)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(AbstractHttpConfigurer::disable)
			.build();
	}

	@Bean
	public JwtEncoder jwtEncoder() {
		return new NimbusJwtEncoder(jwkSource());
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		try {
			return NimbusJwtDecoder.withPublicKey(rsaKey().toRSAPublicKey()).build();
		} catch (JOSEException e) {
			throw new IllegalStateException("[System] JWT decoder 생성에 실패하였습니다", e);
		}
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		JWKSet jwkSet = new JWKSet(rsaKey());
		return new ImmutableJWKSet<>(jwkSet);
	}

	@Bean
	public RSAKey rsaKey() {
		KeyPair keyPair = generateRsaKey();
		RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();
		return new RSAKey.Builder(publicKey)
			.privateKey(privateKey)
			.keyID(UUID.randomUUID().toString())
			.build();
	}

	@Bean
	public PasswordEncoder domainPasswordEncoder() {
		return new PasswordEncoder() {
			private final org.springframework.security.crypto.password.PasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

			@Override
			public String encode(String rawString) {
				return bcryptEncoder.encode(rawString);
			}

			@Override
			public boolean matches(String rawString, String encoded) {
				return bcryptEncoder.matches(rawString, encoded);
			}
		};
	}
}
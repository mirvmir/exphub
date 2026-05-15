package io.github.mirvmir.identity.application;

import io.github.mirvmir.identity.domain.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.time.Duration;
import java.util.List;

@Configuration
@ComponentScan(basePackageClasses = IdentityModuleConfig.class)
public class IdentityModuleConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(CustomUserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder,
                                                       JwtDecoder jwtDecoder) {

        DaoAuthenticationProvider daoProvider =
                new DaoAuthenticationProvider(userDetailsService);
        daoProvider.setPasswordEncoder(passwordEncoder);

        JwtAuthenticationProvider jwtProvider = new JwtAuthenticationProvider(jwtDecoder);
        jwtProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter());

        return new ProviderManager(List.of(daoProvider, jwtProvider));
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyLoader keyLoader) throws Exception {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(
                keyLoader.loadPublicKey()
        ).build();

        OAuth2TokenValidator<Jwt> payloadValidator = jwt -> {
            List<String> roles = jwt.getClaim("role");
            boolean isRole = roles != null && roles.stream().allMatch(Role::isValidAuthority);
            if (isRole) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                            "invalid_token",
                            "Invalid role claim",
                            null
                    )
            );
        };

        OAuth2TokenValidator<Jwt> expirationValidator = new JwtTimestampValidator(Duration.ofSeconds(30));
        // учитываем рассинхрон, плюс должна быть проверка на клиентской части

        // subject здесь не проверяется
        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(payloadValidator, expirationValidator)
        );

        return decoder;
    }
}

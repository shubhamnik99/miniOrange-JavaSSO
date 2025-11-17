package com.example.springssodemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   RelyingPartyRegistrationRepository relyingPartyRegistrationRepository)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Allow public routes (login, register, static)
                .requestMatchers("/", "/login", "/api/users/**", "/register", "/doLogin", "/home", "/admin-dashboard", "/css/**", "/js/**").permitAll()

                // Allow SSO endpoints
                .requestMatchers("/saml2/**", "/oauth2/**", "/sso/**", "/jwt/**").permitAll()

                // All others require auth
                .anyRequest().authenticated()
            )

            // Disable default Spring login, since you're handling it manually
            .formLogin(form -> form.disable())

            // ✅ Enable SAML login for your SSO button
            .saml2Login(saml2 -> saml2
                .defaultSuccessUrl("/home", true)
                .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository)
            )

            // ✅ Enable OAuth login
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
            );

        return http.build();
    }
}
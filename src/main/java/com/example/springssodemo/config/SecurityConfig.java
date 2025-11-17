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
                .requestMatchers("/", "/login", "/api/users/**", "/register", "/doLogin", "/admin-dashboard", "/css/**", "/js/**").permitAll()
                .requestMatchers("/saml2/**", "/oauth2/**", "/sso/**", "/jwt/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .saml2Login(saml2 -> saml2
                .defaultSuccessUrl("/home", true)
                .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository)
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}

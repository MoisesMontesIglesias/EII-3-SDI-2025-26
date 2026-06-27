package com.uniovi.sdi.reservationmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/login/**",
                                "/logout",
                                "/signup",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/script/**",
                                "/webjars/**"
                        )
                        .permitAll()
                        .requestMatchers("/reservas/listado-global/**").hasRole("ADMIN")
                        .requestMatchers("/reservas/mis", "/reservas/*/cancelar").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/reservas/**").hasRole("ADMIN")
                        .requestMatchers("/espacios/*/bloqueos/**").hasRole("ADMIN")
                        .requestMatchers("/espacios/nuevo", "/espacios/*/editar").hasRole("ADMIN")
                        .requestMatchers("/spaces/**", "/espacios/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/"))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .securityContext(securityContext -> securityContext
                        .requireExplicitSave(false)
                );

        return http.build();
    }
}

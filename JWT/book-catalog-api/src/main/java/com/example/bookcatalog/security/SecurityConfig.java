package com.example.bookcatalog.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig
{
    @Bean
    PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain (HttpSecurity http, RestAuthenticationEntryPoint entryPoint, RestAccessDeniedHandler deniedHandler) throws Exception
    {
        http
            .authorizeHttpRequests(auth -> auth
                            .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                            .requestMatchers(HttpMethod.GET, "/auth/csrf", "/books", "/books/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                            .requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
                            .requestMatchers("/books", "/books/**", "/transaction-lab/**").hasRole("ADMIN")
                            .anyRequest().denyAll()
                            )
            .formLogin(form -> form
                    .loginProcessingUrl("/auth/login")
                    .successHandler((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                    .failureHandler((request, response, exception) -> entryPoint.commence(request, response, exception))
                    .permitAll()
                    )
            .logout(logout -> logout
                    .logoutUrl("/auth/logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                    )
            .exceptionHandling(e -> e
                            .authenticationEntryPoint(entryPoint)
                            .accessDeniedHandler(deniedHandler)
                            )
            .csrf(Customizer.withDefaults())
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

}
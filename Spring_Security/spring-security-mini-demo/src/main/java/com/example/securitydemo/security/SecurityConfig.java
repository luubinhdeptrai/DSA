package com.example.securitydemo.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(GET, "/public/hello", "/auth/csrf").permitAll()
                        .requestMatchers(POST, "/auth/register", "/auth/login").permitAll()
                        .requestMatchers(GET, "/auth/me").authenticated()
                        .requestMatchers(GET, "/user/hello").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(GET, "/admin/hello").hasRole("ADMIN")
                        .requestMatchers(POST, "/admin/action", "/admin/audit")
                            .hasRole("ADMIN")
                        .requestMatchers(POST, "/auth/logout").authenticated()
                        .anyRequest().denyAll()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT)
                        )
                        .failureHandler((request, response, exception) ->
                                authenticationEntryPoint.commence(request, response, exception)
                        )
                )
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.changeSessionId())
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT)
                        )
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .requestCache(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}

package com.sharshag.springwebfluxresearch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.sharshag.springwebfluxresearch.service.DevDojoUserDetailsService;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf().disable()
            .authorizeExchange()
            .pathMatchers(HttpMethod.POST, "/animes/**").hasRole("ADMIN")
            .pathMatchers(HttpMethod.PUT, "/animes/**").hasRole("ADMIN")
            .pathMatchers(HttpMethod.DELETE, "/animes/**").hasRole("ADMIN")
            .pathMatchers(HttpMethod.GET, "/animes/**").hasRole("USER")
            .pathMatchers("/v3/api-docs/**", "/webjars/swagger-ui/**", "/swagger-ui.html").permitAll()
            .anyExchange().authenticated()
            .and()
                .formLogin()
                .and()
                .httpBasic()
                .and()
                .build();
    }

    // @Bean
    // public MapReactiveUserDetailsService userDetailsService() {
    //     PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    //     UserDetails user = User.withUsername("user")
    //         .password(passwordEncoder.encode("devdojo"))
    //         .roles("USER")
    //         .build();

    //         UserDetails admin = User.withUsername("admin")
    //         .password(passwordEncoder.encode("devdojo"))
    //         .roles("USER", "ADMIN")
    //         .build();

    //     return new MapReactiveUserDetailsService(user, admin);

    // }

    @Bean
    ReactiveAuthenticationManager authenticationManager(DevDojoUserDetailsService devDojoUserDetailsService) {
        return new UserDetailsRepositoryReactiveAuthenticationManager(devDojoUserDetailsService);
    }
    
}

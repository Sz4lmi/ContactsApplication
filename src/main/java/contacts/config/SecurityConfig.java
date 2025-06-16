package contacts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // REST API-nál nem kell CSRF védelem
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // minden végpontot engedélyezünk
                );
        return http.build();
    }
}

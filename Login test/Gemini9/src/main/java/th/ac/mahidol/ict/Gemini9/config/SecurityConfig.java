package th.ac.mahidol.ict.Gemini9.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // Import for matchers

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public static PasswordEncoder passwordEncoder() {
        // Use BCrypt for strong password hashing
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/login", "/").permitAll()



                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Only ADMIN or ASTRONOMER can access /astronomy-data/**
                        .requestMatchers("/astronomy-data/**").hasAnyRole("ADMIN", "ASTRONOMER")
                        // Only ADMIN or SCIENCE_OBSERVER can access /observations/**
                        .requestMatchers("/observations/**").hasAnyRole("ADMIN", "SCIENCE_OBSERVER")
                        // Welcome page accessible by anyone authenticated (all defined roles)
                        .requestMatchers("/welcome").authenticated()

                        // Require authentication for any other request not specifically matched above
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form

                        .loginPage("/login")

                        .loginProcessingUrl("/login")

                        .defaultSuccessUrl("/welcome", true)

                        .permitAll()
                )
                .logout(logout -> logout

                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))

                        .logoutSuccessUrl("/login?logout")

                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )

                .csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")))

                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}
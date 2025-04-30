package th.ac.mahidol.ict.Gemini_d6.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enable method-level security checks like @PreAuthorize
public class SecurityConfig {

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Allow public access
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/login", "/").permitAll()

                        // --- Configure access control for Science Plan URLs ---
                        .requestMatchers("/science-plans/create", "/science-plans/save").hasRole("ASTRONOMER")
                        .requestMatchers("/science-plans/edit/**", "/science-plans/update/**").hasAnyRole("ADMIN", "ASTRONOMER")
                        .requestMatchers("/view-plans").hasAnyRole("ADMIN", "ASTRONOMER", "SCIENCE_OBSERVER")
                        .requestMatchers("/science-plans/test/**").hasRole("ASTRONOMER")
                        .requestMatchers("/science-plans/submit/**").hasRole("ASTRONOMER")

                        // *** MODIFIED: Allow both Admin and Astronomer to access the delete path ***
                        // The controller logic (@PreAuthorize and internal checks) will handle specific permissions
                        .requestMatchers("/science-plans/delete/**").hasAnyRole("ADMIN", "ASTRONOMER")

                        // Welcome page requires authentication
                        .requestMatchers("/welcome").authenticated()
                        // Any other request requires authentication
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
                // H2 console specific configuration
                .csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")))
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}

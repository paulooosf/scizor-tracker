package app.vercel.paulooosf.scizor_tracker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FiltroSeguranca filtroSeguranca;

    @Autowired
    public SecurityConfig(FiltroSeguranca filtroSeguranca) {
        this.filtroSeguranca = filtroSeguranca;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/autenticar/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/bugs/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/projetos/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/comentarios/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/bugs").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/bugs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/bugs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/projetos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/projetos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/projetos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/comentarios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/comentarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN")
                        
                        .anyRequest().authenticated()
                )
                .addFilterBefore(filtroSeguranca, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

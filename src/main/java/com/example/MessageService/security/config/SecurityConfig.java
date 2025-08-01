package com.example.MessageService.security.config;
import com.example.MessageService.security.jwt.JwtAuthenticationEntryPoint;
import com.example.MessageService.security.jwt.JwtRequestFilter;
import com.example.MessageService.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;
    private final CustomUserDetailsService userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        http

                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

                // --- Authorization Rules ---
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers( "/register", "/login",
                                "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/dashboard/**").hasRole("TENANT")
                        .requestMatchers("/admin/dashboard").hasRole("SUPER_ADMIN")
                        .requestMatchers("/admin/create-admin", "/admin/admins/*/delete").hasRole("SUPER_ADMIN")
                        .requestMatchers("/admin/messages").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/segments/**").hasRole("TENANT")
                        .requestMatchers("/api/tenants/*/users").hasRole("TENANT")
                        .requestMatchers("/api/admins/**").hasRole("ADMIN")
                        .requestMatchers("/api/admins/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/favicon.ico").permitAll()

                        .requestMatchers(HttpMethod.POST , "/api/admins/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT ,"/api/admins/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE , "/api/admins/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET ,"/api/admins/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET , "/api/admins/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        http.authenticationProvider(daoAuthenticationProvider());
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {

            String targetUrl = "/dashboard";

            var authorities = authentication.getAuthorities();

            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
                targetUrl = "/admin/dashboard";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                targetUrl = "/admin/viewer-dashboard";
            }

            response.sendRedirect(request.getContextPath() + targetUrl);
        };
    }
}
package com.you.springsecurity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

//    @Bean
//    public PasswordEncoder passwordEncoder (){
//        return new BCryptPasswordEncoder();
//    }

//    @Bean
//    public UserDetailsService userDetailsService (){
//        UserDetails Admin = User.builder()
//                .username("Admin")
//                .password(passwordEncoder().encode("Admin@123"))
//                .roles("ADMIN")
//                .build();
//        UserDetails userDetails = User.builder()
//                .username("Users")
//                .password(passwordEncoder().encode("user@123"))
//                .roles("USER").build();
//
//        return new InMemoryUserDetailsManager(
//                Admin
//        );
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain (HttpSecurity httpSecurity){
//        httpSecurity.authorizeHttpRequests(
//                auth->auth
//                        .requestMatchers("/api/v1/public/**").permitAll()
//                        .requestMatchers("/api/v1/admin/")
//                        .hasRole("ADMIN")
//        ).httpBasic(Customizer.withDefaults());
//        return  httpSecurity.build();
//    }
//


    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(
                userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationProvider authenticationProvider
    ) {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider
    ) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                )).authorizeHttpRequests(
                        auth->auth
                                .requestMatchers(HttpMethod.POST,"/api/v1/auth/login").permitAll()
                                .requestMatchers("/api/v1/public/**").permitAll()
                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                ).httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
//        return http.authenticationProvider(authenticationProvider)
//                .authorizeHttpRequests(auth->auth
//                        .requestMatchers("/api/v1/public/**").permitAll()
//                        .requestMatchers("/api/v1/admin/**")
//                        .hasRole("ADMIN")
//                        .anyRequest()
//                        .authenticated()).httpBasic(Customizer.withDefaults()).build();

    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
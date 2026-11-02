package com.caleb.SecEx.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityProperties.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration 
@EnableWebSecurity 
public class SecurityConfig {

    @Autowired 
    private UserDetailsService userDetailsService;

    @Bean 
    public SecurityFilterChain securityFilterChain(HttpSecurity http){

        http.csrf(customizer -> customizer.disable()).authorizeHttpRequests(request -> request.anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults())
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
        @Bean 
        public AuthenticationProvider authenticationProvider(){
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider(null);
            provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        //    provider.setUserDetailsService(userDetailsService);
            return provider;
        }
        

    // @Bean
    // public UserDetailsService userDetailsService(){
    //     UserDetails user1 = org.springframework.security.core.userdetails.User
    //         .withDefaultPasswordEncoder()
    //         .username("Rosa")
    //         .password("Human")
    //         .roles("USER")
    //         .build();

    //     UserDetails user2 = org.springframework.security.core.userdetails.User
    //         .withDefaultPasswordEncoder()
    //         .username("karufu")
    //         .password("Human11")
    //         .roles("ADMIN")
    //         .build();

    //     return new InMemoryUserDetailsManager(user1, user2);
    // }

}

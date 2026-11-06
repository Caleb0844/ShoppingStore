package com.caleb.SecEx.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration 
@EnableWebSecurity 
public class SecurityConfig {



    @Autowired 
    private UserDetailsService userDetailsService;

    @Autowired 
    private JwtFilter jwtFilter;
    

    @Bean 
    public SecurityFilterChain securityFilterChain(HttpSecurity http){

        http.csrf(customizer -> customizer.disable()).authorizeHttpRequests(request -> request
            .requestMatchers("/register", "/login")
            .permitAll() 
            .anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults())
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
        @Bean 
        public AuthenticationProvider authenticationProvider(){
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
            provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
            return provider;
        }

        @Bean 
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
           return config.getAuthenticationManager();

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

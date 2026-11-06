package com.caleb.SecEx.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import com.caleb.SecEx.Service.JWTService;
import com.caleb.SecEx.Service.MyUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends  OncePerRequestFilter{

    @Autowired 
    private JWTService jwtService;

    @Autowired 
    ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
                //Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTc5Mzk0Mzg0OCwiZXhwIjoxNzkzOTQzOTIwfQ.LjScHa_i7o7mMItVjW69gQp68CGSKDOnU71pDw97KeE
            String autHeader = request.getHeader("Authorization");
            String token = null;
            String username  = null;

            if(autHeader != null && autHeader.startsWith("Bearer ")){
                token = autHeader.substring(7);
                username = jwtService.extractUsername(token);
            }

            if(username !=null && SecurityContextHolder.getContext().getAuthentication() == null){

                UserDetails userDetails = context.getBean(MyUserDetailsService.class).loadUserByUsername(username);

                if(jwtService.validateToken(token, userDetails)){
                    UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
    }


}

package com.caleb.SecEx.config;

import java.io.IOException;

import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.caleb.SecEx.Service.JWTService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JwtFilter
 */
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

                UserDetails userDetails = context.getBean(UserDetails.class);

                if(jwtService.validateToken(token, userDetails)){

                }
            }
    }


}

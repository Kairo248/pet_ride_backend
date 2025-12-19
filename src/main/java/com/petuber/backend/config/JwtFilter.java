package com.petuber.backend.config;

import com.petuber.backend.auth.JwtUtil;
import com.petuber.backend.user.User;
import com.petuber.backend.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip JWT filter for public endpoints only
        String path = request.getRequestURI();
        if (path.equals("/api/auth/register") || path.equals("/api/auth/login") || path.startsWith("/ws/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        
        System.out.println("🔍 JWT Filter - Request: " + request.getRequestURI());
        System.out.println("🔍 JWT Filter - Authorization header: " + (authHeader != null ? "Present" : "Missing"));

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("🔍 JWT Filter - Token received, length: " + token.length());
            try {
                String email = jwtUtil.extractEmail(token);
                System.out.println("🔍 JWT Filter - Extracted email: " + email);
                User user = userRepository.findByEmail(email).orElse(null);

                if (user != null) {
                    System.out.println("✅ JWT Filter - User found: " + user.getEmail() + " (ID: " + user.getId() + ")");
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    user, null, Collections.emptyList()
                            );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println("✅ JWT Filter - Authentication set successfully");
                } else {
                    System.out.println("❌ JWT Filter - User not found for email: " + email);
                }
            } catch (Exception e) {
                System.out.println("❌ JWT Filter - Token validation failed: " + e.getMessage());
                e.printStackTrace();
                // Invalid token, continue without authentication
                // Spring Security will handle authorization
            }
        } else {
            System.out.println("⚠️ JWT Filter - No valid Authorization header (missing or not Bearer)");
        }

        filterChain.doFilter(request, response);
    }
}


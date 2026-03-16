package com.kits.jklub.config;

import com.kits.jklub.util.JwtUtils;
import com.kits.jklub.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Custom filter to process JWT for authentication on every request.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public JwtAuthFilter(JwtUtils jwtUtils, CustomUserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Get the current request path
        String path = request.getServletPath();

        // ✅ STEP 0: Bypass JWT logic for public authentication endpoints.
        // This ensures the filter doesn't try to validate tokens for guest actions
        // like login, registration, password resets, or contact messages.
        if (path.startsWith("/api/v1/auth/") ||
                path.equals("/api/v1/login") ||
                path.equals("/api/v1/register") ||
                path.equals("/api/v1/contact") ||
                path.startsWith("/api/v1/otp/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String loginIdentifier;

        // 1. Check for Authorization header and JWT format.
        // If header is missing or doesn't start with "Bearer ", pass it to the next filter.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token by removing the "Bearer " prefix (7 characters)
        jwt = authHeader.substring(7);

        // 2. Extract user ID (loginIdentifier) from the token
        try {
            loginIdentifier = jwtUtils.extractUsername(jwt);
        } catch (Exception e) {
            // If token is invalid or expired, continue the chain without authenticating the user.
            // Spring Security will catch the lack of authentication later if the route is protected.
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Validate user and set security context if user is found and not already authenticated.
        if (loginIdentifier != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(loginIdentifier);

            if (jwtUtils.validateToken(jwt, userDetails)) {
                // Token is valid, create a new authentication object
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                // Set authentication details like remote IP address and session ID
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Finalize the authentication by setting it in the Security Context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue with the rest of the filter chain
        filterChain.doFilter(request, response);
    }
}
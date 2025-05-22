package com.web.tech.filter;

import com.web.tech.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    private final List<String> PUBLIC_PATHS = List.of(
            "/", "/home", "/css/", "/js/", "/images/",
            "/landing", "/register", "/login", "/exhibitions", "/collection");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        logger.debug("AuthFilter processing request: " + requestURI);

        // Skip auth check for public paths
        if (isPublicPath(requestURI)) {
            logger.debug("Public path detected, bypassing auth check: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // Get the current authentication from SecurityContext
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        // Check if user has a valid authentication in SecurityContext
        if (existingAuth != null && existingAuth.isAuthenticated() &&
                !(existingAuth.getPrincipal() instanceof String) &&
                !existingAuth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ANONYMOUS"))) {

            logger.debug("User already authenticated in SecurityContext: " + existingAuth.getName());
            logger.debug("Existing authorities: " + existingAuth.getAuthorities());
            filterChain.doFilter(request, response);
            return;
        }

        // Verify session auth
        HttpSession session = request.getSession(false);
        if (session == null) {
            logger.debug("No session found, redirecting to login");
            response.sendRedirect("/login");
            return;
        }

        // Check if session has a user object
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            logger.debug("No user in session, redirecting to login");
            response.sendRedirect("/login");
            return;
        }

        logger.debug("Found user in session: " + sessionUser.getEmail());
        logger.debug("Session user role: " + sessionUser.getRole());

        // Create authorities based on user role (case-insensitive)
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        String userRole = sessionUser.getRole() != null ? sessionUser.getRole().trim() : "USER";
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        logger.debug("Assigned authorities in AuthFilter: " + authorities);

        // Create authentication object
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        sessionUser.getEmail(),
                        null,
                        authorities
                );

        authentication.setDetails(new WebAuthenticationDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.debug("Set authentication in SecurityContext for user: " + sessionUser.getEmail());

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String requestURI) {
        return PUBLIC_PATHS.stream().anyMatch(requestURI::startsWith);
    }
}
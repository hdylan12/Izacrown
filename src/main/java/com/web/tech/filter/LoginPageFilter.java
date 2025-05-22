package com.web.tech.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Order(1) // Make sure this runs before other filters
public class LoginPageFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoginPageFilter.class);

    private final List<String> LOGIN_PATHS = Arrays.asList(
            "/login",
            "/login-process"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Only apply this filter to login pages
        if (isLoginPath(requestURI)) {
            logger.debug("Login path detected: {}", requestURI);

            // Clear security context for login page
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                logger.debug("Clearing authentication for login page: {}", auth.getName());
                SecurityContextHolder.clearContext();
            }

            // Invalidate session for login page
            HttpSession session = request.getSession(false);
            if (session != null && !request.getMethod().equals("POST")) {
                // Don't invalidate during form submission
                try {
                    logger.debug("Invalidating session for login page");
                    session.removeAttribute("user");
                    session.removeAttribute("SPRING_SECURITY_CONTEXT");
                    session.invalidate();
                } catch (IllegalStateException e) {
                    // Session might already be invalidated
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginPath(String requestURI) {
        return LOGIN_PATHS.stream().anyMatch(requestURI::equals);
    }
}
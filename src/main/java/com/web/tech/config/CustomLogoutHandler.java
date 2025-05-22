package com.web.tech.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {
        // Invalidate the session
        HttpSession session = request.getSession(false);
        if (session != null) {
            try {
                // Clear all session attributes first
                session.removeAttribute("user");
                session.removeAttribute("SPRING_SECURITY_CONTEXT");
                session.invalidate();
            } catch (IllegalStateException e) {
                // Session might already be invalidated, ignore
            }
        }

        // Clear security context
        SecurityContextHolder.clearContext();

        // Invalidate cookies except remember-me email cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // Skip the rememberEmail cookie - we want to keep it
                if ("rememberEmail".equals(cookie.getName())) {
                    continue;
                }

                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }
}
package com.springboot.project.community;

import com.springboot.project.community.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();
        
        if (path.equals("/api/auth") ||
            path.startsWith("/api/auth/signup") ||
            path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/refresh") ||
            path.startsWith("/api/auth/check") ||
            path.startsWith("/api/auth/check-email") ||
            path.startsWith("/api/auth/check-nickname") ||
            path.startsWith("/api/auth/test")) {
            return true;
        }

        if ("GET".equals(method) && path.startsWith("/api/boards")) {
            return true;
        }
        
        return true;
    }
}

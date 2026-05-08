package com.example.anvisos.auth.interceptor;

import com.example.anvisos.auth.annotation.RequiredRole;
import com.example.anvisos.auth.service.JwtService;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.enums.UserRole;
import com.example.anvisos.model.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class RoleInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiredRole requiredRole = handlerMethod.getMethodAnnotation(RequiredRole.class);
        if (requiredRole == null) {
            requiredRole = handlerMethod.getBeanType().getAnnotation(RequiredRole.class);
        }

        String authHeader = request.getHeader("Authorization");
        User user = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                Long userId = jwtService.getUserIdFromToken(authHeader);
                user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    request.setAttribute("currentUser", user);
                }
            } catch (Exception ignored) {}
        }

        if (requiredRole == null) {
            return true;
        }

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        UserRole userRole = user.getRole();
        boolean hasPermission = Arrays.asList(requiredRole.value()).contains(userRole);

        if (!hasPermission) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}

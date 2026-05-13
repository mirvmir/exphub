package io.github.mirvmir;

import io.github.mirvmir.common.annotation.RequiresCompletedProfile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CompletedProfileInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        boolean required =
                handlerMethod.hasMethodAnnotation(RequiresCompletedProfile.class)
                        || handlerMethod.getBeanType().isAnnotationPresent(RequiresCompletedProfile.class);

        if (!required) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Boolean completed = jwtAuth.getToken().getClaim("profileCompleted");

        if (!Boolean.TRUE.equals(completed)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                      "code": "PROFILE_REQUIRED",
                      "message": "Необходимо заполнить профиль"
                    }
                    """);
            return false;
        }

        return true;
    }
}
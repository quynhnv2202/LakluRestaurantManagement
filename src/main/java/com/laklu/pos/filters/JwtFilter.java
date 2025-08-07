package com.laklu.pos.filters;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.valueObjects.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtGuard jwtGuard;
    private final UserDetailsService userService;

    private final HandlerExceptionResolver resolver;

    public JwtFilter(JwtGuard jwtGuard, UserDetailsService userService, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtGuard = jwtGuard;
        this.userService = userService;
        this.resolver = resolver;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        String subject = null;
        try{
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
                subject = jwtGuard.extractSubject(token);
            }
            if (subject != null && !JwtGuard.isAuthenticated()) {
                UserPrincipal userPrincipal = (UserPrincipal) userService.loadUserByUsername(subject);

                if (jwtGuard.validateToken(token, userPrincipal)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userPrincipal, null,
                            userPrincipal.getAuthorities()
                    );
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            this.resolver.resolveException(request, response, null, e);
        }
    }
}

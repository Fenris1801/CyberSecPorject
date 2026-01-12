package fr.unice.polytech.securenotes.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Order(1)
public class InternalAuthFilter extends OncePerRequestFilter {

    private final String internalSecret;

    public InternalAuthFilter(
            @Value("${REPLICATION_SECRET}") String internalSecret) {
        this.internalSecret = internalSecret;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/internal")) {
            String header = request.getHeader("X-Internal-Secret");

            if (!internalSecret.equals(header)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);

                System.out.println("Blocked internal call from " + request.getRemoteAddr() + " to " + request.getRequestURI());

                return;
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "INTERNAL_REPLICATION",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}

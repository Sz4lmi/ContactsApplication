package contacts.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import static contacts.config.SecurityConstants.SECRET_KEY;
import java.io.IOException;
import java.util.Collections;

/**
 * Filter for JWT authentication.
 * Intercepts requests and validates JWT tokens, setting up Spring Security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final SecretKey SECRET_KEY = SecurityConstants.SECRET_KEY;

    /**
     * Filters incoming requests and applies JWT authentication.
     * Skips authentication for paths starting with /api/auth.
     *
     * @param request The HTTP request
     * @param response The HTTP response
     * @param filterChain The filter chain
     * @throws ServletException If a servlet exception occurs
     * @throws IOException If an I/O exception occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Log the request path
        logger.debug("JWT FILTER - PATH: {}", request.getRequestURI());

        // Skip filter for auth endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = parseJwt(request);

        // Log the JWT token
        logger.debug("JWT FILTER - JWT: {}", jwt);

        if (jwt != null) {
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY)
                        .parseClaimsJws(jwt)
                        .getBody();

                // Log successful token decoding
                logger.debug("JWT FILTER - USER: {}", claims.getSubject());
                logger.debug("JWT FILTER - ROLE: {}", claims.get("role", String.class));

                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username, null,
                                Collections.singleton(new SimpleGrantedAuthority(role))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                logger.debug("JWT FILTER - ERROR: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            logger.debug("JWT FILTER - NO TOKEN");
        }

        filterChain.doFilter(request, response);
    }



    /**
     * Extracts the JWT token from the Authorization header.
     *
     * @param request The HTTP request
     * @return The JWT token or null if not found
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}

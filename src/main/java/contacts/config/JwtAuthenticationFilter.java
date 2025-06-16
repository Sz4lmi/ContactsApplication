package contacts.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final SecretKey SECRET_KEY = SecurityConstants.SECRET_KEY;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // LOG 1 - Minden kérésnél lásd az URL-t
        System.out.println("JWT FILTER - PATH: " + request.getRequestURI());

        // KIHAGYJUK a filtert az auth végpontoknál!
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = parseJwt(request);

        // LOG 2 - Lásd a JWT-t, ami jött
        System.out.println("JWT FILTER - JWT: " + jwt);

        if (jwt != null) {
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY)
                        .parseClaimsJws(jwt)
                        .getBody();

                // LOG 3 - Sikeres dekódolás után lásd user/role-t
                System.out.println("JWT FILTER - USER: " + claims.getSubject());
                System.out.println("JWT FILTER - ROLE: " + claims.get("role", String.class));

                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username, null,
                                Collections.singleton(new SimpleGrantedAuthority(role))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                System.out.println("JWT FILTER - HIBA: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            System.out.println("JWT FILTER - NINCS TOKEN");
        }

        filterChain.doFilter(request, response);
    }



    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
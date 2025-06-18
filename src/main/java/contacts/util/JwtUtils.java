package contacts.util;

import contacts.config.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for JWT token operations.
 * Provides methods for extracting information from JWT tokens.
 */
public class JwtUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    
    /**
     * Extracts the user ID from the JWT token in the request.
     *
     * @param request The HTTP request containing the JWT token
     * @return The user ID or null if not found or token is invalid
     */
    public static Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(SecurityConstants.SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody();

                // Get userId from claims
                Integer userId = claims.get("userId", Integer.class);
                return userId != null ? userId.longValue() : null;
            } catch (Exception e) {
                // Token validation failed
                logger.debug("Token validation failed: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * Extracts the user role from the JWT token in the request.
     *
     * @param request The HTTP request containing the JWT token
     * @return The user role or null if not found or token is invalid
     */
    public static String getRoleFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(SecurityConstants.SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody();

                // Get role from claims
                return claims.get("role", String.class);
            } catch (Exception e) {
                // Token validation failed
                logger.debug("Token validation failed: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }
}
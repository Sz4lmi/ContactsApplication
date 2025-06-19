package contacts.config;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public class SecurityConstants {
    public static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "this_is_my_very_long_secret_key_that_should_be_working_for_HS256_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!!!".getBytes()
            //TODO
            //This key should not be hardcoded in production code
            //Use a secure key management solution or environment variable
            //This is just for demonstration purposes
    );
}

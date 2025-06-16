package contacts.config;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public class SecurityConstants {
    public static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "this_is_my_very_long_secret_key_that_should_be_working_for_HS256_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!!!".getBytes()
    );
}

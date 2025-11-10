package lt.creditco.cupa.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";
    
    public static final String CREDITCO = "ROLE_CREDITCO";
    
    public static final String MERCHANT = "ROLE_MERCHANT";

    private AuthoritiesConstants() {}
}

package backend.authentication;

import backend.database.DatabaseConnector;
import backend.exceptions.*;
import backend.model.User;
import backend.model.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.logging.log4j.LogManager;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manges user login, registration and, authorisation. No cache of users is stored on this node.
 *
 * @author John, Danny, Flynn
 * @version 8
 * @see NonceManager
 */
public class UserManager {

    /**
     * The payload of the JWT is the type, this is the access type.
     *
     * @since 4
     */
    public final static String ACCESS_TOKEN = "access";
    /**
     * The expiration tag for JWTs
     *
     * @since 4
     */
    public final static String EXPIRATION_TAG = "expires";
    /**
     * The subject tag for JWTs
     *
     * @since 4
     */
    public final static String SUBJECT_TAG = "subject";
    /**
     * The tag that represents the type
     *
     * @see #ACCESS_TOKEN
     * @see #REFRESH_TOKEN
     * @since 4
     */
    public final static String TYPE_TAG = "type";
    /**
     * The maximum age for the refresh token
     *
     * @since 8
     */
    private final static long REFRESH_TOKEN_EXPIRES = 60L * 60L * 24L * 365L;
    /**
     * The maximum age for the access token
     *
     * @since 8
     */
    private final static long ACCESS_TOKEN_EXPIRES = 60L * 15L;
    /**
     * The payload of the JWT is the type, this is the refresh type.
     *
     * @since 4
     */
    private final static String REFRESH_TOKEN = "refresh";
    /**
     * THe length of a salt.
     *
     * @since 1
     */
    private final static int SALT_LENGTH = 255;

    /**
     * A set of chars to use for use in a salt.
     *
     * @since 1
     */
    private final static char[] SALT_CHARS = "abdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789/*-+,.?/<>:@~;'#"
            .toCharArray();

    /**
     * The database connection that is used to connect.
     *
     * @since 1
     */
    private final DatabaseConnector connector;

    /**
     * The nonce manager that issues and, verifies nonces.
     *
     * @since 1
     */
    private final NonceManager nonceManager;

    /**
     * A secure random number generator for salting hashes
     *
     * @since 1
     */
    private final SecureRandom random;

    /**
     * JWT secret key
     *
     * @since 3
     */
    private final byte[] secret;

    /**
     * Setup a logger
     *
     * @since 6
     */
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * Inits a user manager object.
     *
     * @param connector the database connector
     * @param secret    the secret key for the JWT
     * @since 3
     */
    public UserManager(DatabaseConnector connector, String secret) {
        this.connector = connector;
        this.nonceManager = new NonceManager();
        this.random = new SecureRandom();
        this.secret = secret.getBytes();

        (new Thread(() -> {
            while (true) {
                nonceManager.removeExpiredNonces();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Nonce expiry thread")).start();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        final String pwd = "noiqwawihd98whefsdfsdfsdfddsfsdfsdfewuifnskdaskda" + "dnlkadnoashdiashdiup";
        final MessageDigest md = MessageDigest.getInstance("SHA-512");
        final byte[] hash = md.digest(pwd.getBytes(StandardCharsets.UTF_8));

        // Convert to a string
        final BigInteger number = new BigInteger(1, hash);

        System.out.println(number.toString(16));
    }

    /**
     * Generates a random salt for salting hashes with.
     *
     * @return returns a random salt for salting hashes with
     * @see #SALT_LENGTH
     * @since 1
     */
    private String getSalt() {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < SALT_LENGTH; i++) {
            final char r = SALT_CHARS[Math.abs(this.random.nextInt()) % SALT_CHARS.length];
            sb.append(r);
        }

        return sb.toString();
    }

    /**
     * Hashes a password with a given salt.
     *
     * @param password the password to hashi
     * @param salt     the password's salt
     * @return the salted password as a hex string
     * @throws NoSuchAlgorithmException thrown by java when it fails to find sha 512
     */
    private String hashPassword(final String password, final String salt) throws NoSuchAlgorithmException {
        final String pwd = password + salt;
        final MessageDigest md = MessageDigest.getInstance("SHA-512");
        final byte[] hash = md.digest(pwd.getBytes(StandardCharsets.UTF_8));

        // Convert to a string
        final BigInteger number = new BigInteger(1, hash);

        return number.toString(16);
    }

    /**
     * A wrapper for the nonce manager that returns a nonce
     *
     * @return a nonce
     * @throws NonceIssueException fails when a nonce cannot be issued
     * @see NonceManager#issueNonce()
     * @since 1
     */
    public long getNonce() throws NonceIssueException {
        return this.nonceManager.issueNonce();
    }

    /**
     * Method to register a user and, returns a refresh token jwt if the operation was successful.
     *
     * @param newUser  new user object to add to system.
     * @param password the user's password
     * @param nonce    to check validity of request.
     * @return a refresh token jwt
     * @throws InvalidNonceException    in case the nonce is invalid.
     * @throws NoSuchAlgorithmException thrown by java as it is old
     * @throws RegistrationException    thrown when any error occurs during registration
     * @since 8
     */
    public String register(final User newUser, final String password, final long nonce) throws InvalidNonceException,
            NoSuchAlgorithmException, RegistrationException {
        if (!this.nonceManager.isNonceValid(nonce)) {
            logger.info(String.format("A new user \"%s\" is being registered but has an invalid nonce", newUser.getEmail()));
            throw new InvalidNonceException();
        }

        /*
         * Regex: 1 mandatory '@'; only dots '.' and alphanumeric; no consecutive dots '.'; email cannot start or end
         * with a dot '.'
         */
        final String emailRegex = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        /*
         * Regex: Format must be "xxxxx xxxxxx" and all digits
         */
        final String phoneNumRegex = "^\\s?\\d(\\s|\\d)+$";
        /*
         * Regex: Disallows numbers and some wildcards. Whitespace, hyphen and apostrophe allowed.
         */
        final String nameRegex = "(?i)(^[a-z])((?![ .,'-]$)[a-z .,'-]){0,24}$";

        // Validate user email
        if (!newUser.getEmail().matches(emailRegex)) {
            logger.error(String.format("A new user \"%s\" is being registered but has an invalid email", newUser.getEmail()));
            throw new RegistrationException();
        }

        // Validate user phone number
        if (!newUser.getPhoneNumber().matches(phoneNumRegex)) {
            logger.error(String.format("A new user \"%s\" is being registered but has an invalid phone number", newUser.getPhoneNumber()));
            throw new RegistrationException();
        }

        // Validate user first and last names
        if (!newUser.getFirstname().matches(nameRegex) && !newUser.getSurname().matches(nameRegex)) {
            logger.error(String.format("A new user \"%s\" \"%s\" is being registered but has an invalid first or surname", newUser.getFirstname(), newUser.getSurname()));
            throw new RegistrationException();
        }

        final String passwordSalt = this.getSalt();
        final String passwordHash = this.hashPassword(password, passwordSalt);
        final AtomicBoolean successful = new AtomicBoolean(true);

        try {
            this.connector.runOnDatabase((conn) -> {
                try {
                    PreparedStatement registerUserStatement =
                            conn.prepareStatement("INSERT INTO users (userid, firstname, lastname, email, phoneno, usetype, password, salt) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?);");

                    registerUserStatement.setObject(1, newUser.getUUID());
                    registerUserStatement.setString(2, newUser.getFirstname());
                    registerUserStatement.setString(3, newUser.getSurname());
                    registerUserStatement.setString(4, newUser.getEmail());
                    registerUserStatement.setString(5, newUser.getPhoneNumber());
                    registerUserStatement.setInt(6, newUser.getUserType().getCode());
                    registerUserStatement.setString(7, passwordHash);
                    registerUserStatement.setString(8, passwordSalt);

                    registerUserStatement.execute();
                } catch (SQLException e) {
                    logger.info(String.format("A new user \"%s\" is being registered due to %s",
                            newUser.getEmail(),
                            Arrays.toString(e.getStackTrace())));
                    logger.error(e);
                    successful.set(false);
                }
            });
        } catch (SQLException e) {
            logger.info(String.format("A new user \"%s\" is being registered due to %s",
                    newUser.getEmail(),
                    Arrays.toString(e.getStackTrace())));
            logger.error(e);
            throw new RegistrationException();
        }

        if (!successful.get()) {
            logger.info(String.format("A new user \"%s\" was not registered",
                    newUser.getEmail()));
            throw new RegistrationException();
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(EXPIRATION_TAG, String.valueOf((System.currentTimeMillis() / 1000L) + REFRESH_TOKEN_EXPIRES));
        claims.put(SUBJECT_TAG, newUser.getUUID().toString());
        claims.put(TYPE_TAG, REFRESH_TOKEN);
        logger.info(String.format("A new user \"%s\" was registered",
                newUser.getEmail()));

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, this.secret)
                .compact();
    }

    /**
     * Login method.
     *
     * @param email    given email.
     * @param password given password.
     * @param nonce    given nonce.
     * @return returns a refresh token JWT
     * @throws InvalidNonceException    thrown when the nonce is not valid
     * @throws LoginException           thrown when the user cannot login for <b>any reason</b>
     * @throws NoSuchAlgorithmException thrown by java as it is old
     * @since 1
     */
    public String login(final String email, final String password, final long nonce) throws InvalidNonceException,
            LoginException, NoSuchAlgorithmException {
        logger.info(String.format("User %s is trying to login", email));

        if (!this.nonceManager.isNonceValid(nonce)) {
            logger.info(String.format("User %s has an invalid nonce", email));
            throw new InvalidNonceException();
        }

        final String[] dbSalt = {null}, dbPassword = {null};
        final UUID[] uuid = {null};
        final AtomicBoolean userFound = new AtomicBoolean(true);
        final AtomicBoolean genericFailure = new AtomicBoolean(false);

        try {
            this.connector.runOnDatabase((conn) -> {
                try {
                    PreparedStatement getHashAndSalt = conn.prepareStatement("SELECT userid, password, salt FROM users WHERE email = ?;");
                    getHashAndSalt.setString(1, email);
                    ResultSet hashAndSalt = getHashAndSalt.executeQuery();
                    if (hashAndSalt.next()) {
                        uuid[0] = UUID.fromString(hashAndSalt.getString(1));
                        dbPassword[0] = hashAndSalt.getString(2);
                        dbSalt[0] = hashAndSalt.getString(3);
                    } else {
                        userFound.set(false);
                    }

                } catch (SQLException e) {
                    genericFailure.set(true);
                    logger.info(String.format("Users %s cannot login in due to %s",
                            email,
                            Arrays.toString(e.getStackTrace())));
                    logger.error(e);
                    e.printStackTrace();
                }
            });
        } catch (SQLException e) {
            logger.info(String.format("User %s cannot login due to %s",
                    email,
                    Arrays.toString(e.getStackTrace())));
            throw new LoginException();
        }

        if (genericFailure.get() || !userFound.get()) {
            logger.info(String.format("User %s was not logged in (not found maybe due to sql fail)", email));
            throw new LoginException();
        }

        String userHashed = this.hashPassword(password, dbSalt[0]);
        if (!userHashed.equals(dbPassword[0])) {
            logger.info(String.format("User %s was not logged in due to wrong password", email));
            throw new LoginException();
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(EXPIRATION_TAG, String.valueOf((System.currentTimeMillis() / 1000L) + REFRESH_TOKEN_EXPIRES));
        claims.put(SUBJECT_TAG, uuid[0].toString());
        claims.put(TYPE_TAG, REFRESH_TOKEN);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, this.secret)
                .compact();
    }

    /**
     * Finds a user from the database
     *
     * @param uuid
     * @return the user object
     * @throws UserNotFoundException thrown when the user is not found
     * @since 8
     */
    public User getUser(final UUID uuid) throws UserNotFoundException, SQLException {
        final String[] firstname = new String[1];
        final String[] surname = new String[1];
        final String[] email = new String[1];
        final String[] phoneNumber = new String[1];
        final UserType[] type = {UserType.CUSTOMER};

        AtomicBoolean found = new AtomicBoolean(false);
        this.connector.runOnDatabase((conn) -> {
            try {
                PreparedStatement ps = conn.prepareStatement("select firstname, lastname, email, phoneno, usetype from users where userid = ?;");
                ps.setObject(1, uuid);

                ResultSet rs = ps.executeQuery();
                rs.next();

                firstname[0] = rs.getString(1);
                surname[0] = rs.getString(2);
                email[0] = rs.getString(3);
                phoneNumber[0] = rs.getString(4);
                type[0] = UserType.from(rs.getInt(5));
                found.set(true);
            } catch (Exception e) {
                logger.error(e);
            }
        });

        if (!found.get()) {
            throw new UserNotFoundException();
        }

        return new User(uuid, firstname[0], surname[0], email[0], phoneNumber[0], type[0]);
    }

    /**
     * Gets the UUID from the jwt
     *
     * @param jwt the jwt to extract the uuid from
     * @since 7
     */
    public UUID getUUIDFromJwt(String jwt) {
        Claims claims = Jwts.parser().setSigningKey(this.secret).parseClaimsJws(jwt).getBody();
        return UUID.fromString((String) claims.get(SUBJECT_TAG));
    }

    /**
     * Validates that a JWT is valid and not expired.
     *
     * @param jwt the base64 jwt string
     * @return whether the jwt is valid and not expired
     * @throws JwtException thrown when the jwt is not valid
     * @since 4
     */
    public boolean isValid(String jwt) throws JwtException {
        Claims claims = Jwts.parser().setSigningKey(this.secret).parseClaimsJws(jwt).getBody();
        return Long.parseLong((String) claims.get(EXPIRATION_TAG)) >= (System.currentTimeMillis() / 1000L);
    }

    /**
     * Used to issue an access token to the user from their refresh token
     *
     * @param jwt   the refresh token jwt
     * @param nonce a nonce to prevent replay attacks
     * @return a new jwt, the access token that lasts a shorter amount of time
     * @throws InvalidNonceException thrown when the nonce is not valid
     * @throws JwtException          thrown when the jwt is not valid
     * @throws LoginException        thrown when another error occurs issuing the token
     * @since 4
     */
    public String useRefresh(final String jwt, final long nonce) throws InvalidNonceException, JwtException, LoginException {
        if (!this.nonceManager.isNonceValid(nonce)) {
            logger.info("A user tried using a jwt to refresh with an invalid nonce");
            throw new InvalidNonceException();
        }

        if (!this.isValid(jwt)) {
            logger.info("A user tried using a jwt to refresh with an invalid jwt");
            throw new LoginException();
        }

        Claims claims = Jwts.parser().setSigningKey(this.secret).parseClaimsJws(jwt).getBody();
        String type = (String) claims.get(TYPE_TAG);
        if (!type.equals(REFRESH_TOKEN)) {
            logger.info("A user tried using a jwt to refresh with an access token");
            throw new LoginException();
        }

        String uuid = (String) claims.get(SUBJECT_TAG);

        // Now we have a valid nonce, jwt and, know it of the correct type we can issue an access token
        Map<String, Object> newClaims = new HashMap<>();
        newClaims.put(EXPIRATION_TAG, String.valueOf((System.currentTimeMillis() / 1000) + ACCESS_TOKEN_EXPIRES));
        newClaims.put(SUBJECT_TAG, uuid);
        newClaims.put(TYPE_TAG, ACCESS_TOKEN);
        logger.info(String.format("A user with uuid %s was issued an access token", uuid));

        return Jwts.builder()
                .setClaims(newClaims)
                .signWith(SignatureAlgorithm.HS512, this.secret)
                .compact();
    }

    /**
     * Checks an access token and nonce pair for avalidity.
     *
     * @param jwt   access token
     * @param nonce a recent nonce
     * @return whether the pair is valid
     */
    public boolean isAccessValid(final String jwt, final long nonce) {
        if (!this.nonceManager.isNonceValid(nonce)) {
            return false;
        }

        if (!this.isValid(jwt)) {
            logger.info("A user tried using a jwt to access which was invalid");
        }

        Claims claims = Jwts.parser().setSigningKey(this.secret).parseClaimsJws(jwt).getBody();
        String type = (String) claims.get(TYPE_TAG);

        if (!type.equals(ACCESS_TOKEN)) {
            logger.info("A user tried using a jwt to access which was a refresh token");
        }

        return true;
    }

    /**
     * Tests whether a nonce is valid.
     *
     * @param nonce the nonce to test
     * @return whether the nonce is valid
     * @since 5
     */
    public boolean isNonceValid(long nonce) {
        return this.nonceManager.isNonceValid(nonce);
    }
}

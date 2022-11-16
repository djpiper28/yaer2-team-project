package backend;

import backend.authentication.UserManager;
import backend.database.DatabaseConnector;
import backend.exceptions.InvalidNonceException;
import backend.exceptions.LoginException;
import backend.exceptions.NonceIssueException;
import backend.exceptions.RegistrationException;
import backend.model.User;
import backend.model.UserType;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static backend.authentication.UserManager.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserManagerTest {

  private static final String FIRSTNAME = "Dave",
      SURNAME = "Cohen",
      EMAIL = "dave.coheng@davemail.com",
      PHONE_NUMBER = "05555 555555",
      PASSWORD = "HelloWorld123.";

  private final static DatabaseConnector conn = TestUtils.getDatabaseConnector();
  private static UserManager userManager;
  private static final String SECRET = "secret";

  @BeforeAll static void initUserManager() {
    userManager = new UserManager(conn, SECRET);
  }

  @Test void testRegisterUser() throws Exception {
    final long nonce = userManager.getNonce();
    final User user = new User(UUID.randomUUID(), FIRSTNAME, SURNAME, EMAIL, PHONE_NUMBER, UserType.CUSTOMER);

    // Test the JWT
    final String jwt = userManager.register(user, PASSWORD, nonce);
    assertTrue(userManager.isValid(jwt));
    final String accessToken = userManager.useRefresh(jwt, userManager.getNonce());
    assertTrue(userManager.isValid(accessToken));

    // Test that duplication is banned
    final long nonce2 = userManager.getNonce();
    assertThrows(RegistrationException.class, () -> {
      userManager.register(user, PASSWORD, nonce2);
    });

  }

  @Test void testRegisterInvalidNonce() throws Exception {
    final long invalidNonce = 5L;
    final User user = new User(UUID.randomUUID(), FIRSTNAME, SURNAME, EMAIL, PHONE_NUMBER, UserType.CUSTOMER);
    final String password = "HelloWorld123.";

    assertThrows(InvalidNonceException.class, () -> {
      userManager.register(user, password, invalidNonce);
    });
  }

  @Test void testInvalidJwtsAreRejected() {
    assertThrows(JwtException.class, () -> {
      userManager.useRefresh("very bad jwt", userManager.getNonce());
    });
  }

  @Test void testExpiredTokensDontWork() {
    final Map<String, Object> claims = new HashMap<>();
    claims.put(EXPIRATION_TAG, String.valueOf((System.currentTimeMillis() / 1000) - 100));
    claims.put(SUBJECT_TAG, UUID.randomUUID());
    claims.put(TYPE_TAG, ACCESS_TOKEN);

    final String jwt = Jwts.builder()
            .setClaims(claims)
            .signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
            .compact();

    assertFalse(userManager.isValid(jwt));
  }

  @Test void testIsAccessTokenValid() throws NonceIssueException {
    final Map<String, Object> claims = new HashMap<>();
    claims.put(EXPIRATION_TAG, String.valueOf(System.currentTimeMillis()));
    claims.put(SUBJECT_TAG, UUID.randomUUID());
    claims.put(TYPE_TAG, ACCESS_TOKEN);

    final String jwt = Jwts.builder()
            .setClaims(claims)
            .signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
            .compact();

    assertTrue(userManager.isAccessValid(jwt, userManager.getNonce()));
  }

  @Test void testRegisterInvalidEmail() throws Exception, RegistrationException {
    final long nonce = userManager.getNonce();
    final User user = new User(UUID.randomUUID(), FIRSTNAME, SURNAME, "email@gmail.com.", PHONE_NUMBER, UserType.CUSTOMER);
    final String password = "HelloWorld123.";

    assertThrows(RegistrationException.class, () -> {
      userManager.register(user, password, nonce);
    });
  }

  @Test void testRegisterInvalidPhoneNumber() throws Exception {
    final long nonce = userManager.getNonce();
    final User user = new User(UUID.randomUUID(), FIRSTNAME, SURNAME, EMAIL, "notANumber", UserType.CUSTOMER);
    final String password = "HelloWorld123.";

    assertThrows(RegistrationException.class, () -> {
      userManager.register(user, password, nonce);
    });
  }

  @Test void testRegisterInvalidNames() throws Exception {
    final long nonce = userManager.getNonce();
    final User user = new User(UUID.randomUUID(), "123", "Not@a&realName", EMAIL, PHONE_NUMBER, UserType.CUSTOMER);
    final String password = "HelloWorld123.";

    assertThrows(RegistrationException.class, () -> {
      userManager.register(user, password, nonce);
    });
  }

  @AfterAll static void testLogin() throws Exception {
    final long nonce = userManager.getNonce();
    final String jwt = userManager.login(EMAIL, PASSWORD, nonce);
    assertTrue(userManager.isValid(jwt));

    final String accessToken = userManager.useRefresh(jwt, userManager.getNonce());
    assertTrue(userManager.isValid(accessToken));

    assertThrows(InvalidNonceException.class, ()-> {
      userManager.useRefresh(jwt, 5L);
    });

    assertThrows(LoginException.class, () -> {
      userManager.useRefresh(accessToken, userManager.getNonce());
    });
  }

  @AfterAll static void testUserNotFound() throws Exception {
    final long nonce = userManager.getNonce();
    assertThrows(LoginException.class, () -> {
      userManager.login(EMAIL + "dada", PASSWORD, nonce);
    });
  }

  @AfterAll static void testWrongPassword() throws Exception {
    final long nonce = userManager.getNonce();
    assertThrows(LoginException.class, () -> {
      userManager.login(EMAIL, PASSWORD + "testing 2123213123", nonce);
    });
  }

  @AfterAll static void testNonceValid() throws NonceIssueException {
    final long nonce = userManager.getNonce();
    assertTrue(userManager.isNonceValid(nonce));
  }

}

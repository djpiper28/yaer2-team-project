package backend;

import backend.model.User;
import backend.model.UserType;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {
    private static final UUID uuid = UUID.randomUUID();
    private static final String FIRSTNAME = "Dave",
            SURNAME = "Cohen",
            EMAIL = "dave.cohen@davemail.com",
            PHONE_NUMBER = "primitive obsession";
    private static final UserType USER_TYPE = UserType.CUSTOMER;

    @Test void testConstructor() {
        new User(uuid, FIRSTNAME, SURNAME, EMAIL, PHONE_NUMBER, USER_TYPE);
    }

    @Test void testGetters() {
        final User user = new User(uuid, FIRSTNAME, SURNAME, EMAIL, PHONE_NUMBER, USER_TYPE);

        assertEquals(user.getUUID(), uuid);
        assertEquals(user.getFirstname(), FIRSTNAME);
        assertEquals(user.getSurname(), SURNAME);
        assertEquals(user.getEmail(), EMAIL);
        assertEquals(user.getPhoneNumber(), PHONE_NUMBER);
        assertEquals(user.getUserType(), USER_TYPE);
    }

    @Test void testAsJson() {
        final User user = new User(uuid, FIRSTNAME, SURNAME, EMAIL, PHONE_NUMBER, USER_TYPE);
        final JSONObject object = user.asJson();

        assertEquals(user.getUUID().toString(), object.get("uuid"));
        assertEquals(user.getFirstname(), object.get("firstname"));
        assertEquals(user.getSurname(), object.get("surname"));
        assertEquals(user.getEmail(), object.get("email"));
        assertEquals(user.getPhoneNumber(), object.get("phonenumber"));
        assertEquals(user.getUserType().toString(), object.get("user-type"));
    }
}

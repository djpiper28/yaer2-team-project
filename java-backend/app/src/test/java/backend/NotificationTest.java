package backend;

import backend.authentication.UserManager;
import backend.model.Notification;
import backend.model.User;
import backend.model.UserType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotificationTest {
    static UUID uuid;
    @BeforeAll static void testAddToDb() throws Exception {
        final UserManager um = new UserManager(TestUtils.getDatabaseConnector(), "secret");
        uuid = UUID.randomUUID();
        final User user = new User(uuid, "Dave", "Cohen", "dc56485674563486@test.com", "048646511", UserType.WAITER_STAFF);
        um.register(user, "mdaljdoiwh", um.getNonce());

        final UUID uid = UUID.randomUUID();
        final String body = "test";
        final int tableno = 5;
        final Notification n = new Notification(uid, uuid, body, tableno);
        n.addToDatabase(TestUtils.getDatabaseConnector());
    }

    @Test void testConstructorAndGetters() {
        final UUID uid = UUID.randomUUID(), custid = UUID.randomUUID();
        final String body = "test";
        final int tableno = 5;
        final Notification n = new Notification(uid, custid, body, tableno);

        assertEquals(custid, n.getCustomerUUID());
        assertEquals(uid, n.getUUID());
        assertEquals(tableno, n.getTableNumber());
        assertEquals(body, n.getBody());
    }

    @Test void testRmFromDb() throws Exception {
        final UserManager um = new UserManager(TestUtils.getDatabaseConnector(), "secret");
        final UUID uuid = UUID.randomUUID();
        final User user = new User(uuid, "Dave", "Cohen", "32984y398efyhehsludfyeifhse98fy@test.com", "0486234211", UserType.WAITER_STAFF);
        um.register(user, "mdaljdoiwh", um.getNonce());

        Notification.rmFromDatabase(TestUtils.getDatabaseConnector(), uuid);
    }
}

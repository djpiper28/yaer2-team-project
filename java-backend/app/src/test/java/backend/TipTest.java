package backend;

import backend.authentication.UserManager;
import backend.database.DatabaseConnector;
import backend.exceptions.InvalidNonceException;
import backend.exceptions.InvalidOrderException;
import backend.exceptions.NonceIssueException;
import backend.exceptions.RegistrationException;
import backend.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TipTest {
    private static User user;
    private static final List<OrderLine> orderLines = new LinkedList<>();
    private static final String FIRSTNAME = "asdasda",
            SURNAME = "asdads",
            EMAIL = "testuse576576r1234@gmail.com",
            PHONE_NUMBER = "07955 569555",
            PASSWORD = "asdadas.";

    private final static DatabaseConnector dbConn = TestUtils.getDatabaseConnector();
    private static UserManager userManager;
    private static final String SECRET = "secret";
    private static final UUID ORDER_ID = UUID.randomUUID();

    @BeforeAll
    static void initUser() throws NonceIssueException, RegistrationException, InvalidNonceException, NoSuchAlgorithmException {
        userManager = new UserManager(TestUtils.getDatabaseConnector(), "asjdahdoiashdiashdisu");
        final long nonce = userManager.getNonce();
        user = new User(UUID.randomUUID(), FIRSTNAME, SURNAME, EMAIL, PHONE_NUMBER, UserType.CUSTOMER);

        final String jwt = userManager.register(user, PASSWORD, nonce);
        assertTrue(userManager.isValid(jwt));
    }

    @BeforeAll static void initOrderLines() throws SQLException {
        TestUtils.getDatabaseConnector().runOnDatabase((c) -> {
            try {

                int QUANTITY = 5;
                final String REQUESTS = "Extra Cheese";

                for (MenuItem item : MenuItem.getMenuItems(TestUtils.getDatabaseConnector(), true)) {
                    if (item.isActive() && item.inStock(c)) {
                        orderLines.add(new OrderLine(item, QUANTITY, REQUESTS));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                fail(e);
            }
        });
    }

    @Test void testConstructor() {
        final double testAmount = 2.50;
        final UUID testUuid = UUID.randomUUID();

        Tip tip = new backend.model.Tip(testUuid, testAmount);
    }

    @Test void testGetters() {
        final double testAmount = 2.50;
        final UUID testUuid = UUID.randomUUID();

        Tip tip = new Tip(testUuid, testAmount);
        assertEquals(testUuid, tip.getUUID());
        assertEquals(testAmount, tip.getAmount());
    }

    @Test void testDatabaseReadWrite() throws InvalidOrderException, SQLException {
        //order details
        final UUID custUuid = user.getUUID();
        final OrderStatus status = OrderStatus.UNCONFIRMED;
        final LocalDateTime made = LocalDateTime.now();
        final LocalDateTime updated = LocalDateTime.now().minusDays(180);
        final int tableno = 5;
        //tip details
        final double testAmount = 2.50;

        final Order order = new Order(ORDER_ID, custUuid, orderLines, status, made, updated, tableno);
        order.addToDatabase(TestUtils.getDatabaseConnector());
        Tip tip = new Tip(ORDER_ID, testAmount);
        tip.addToDatabase(TestUtils.getDatabaseConnector());
        final Tip tipCopy = Tip.fromUUID(ORDER_ID, TestUtils.getDatabaseConnector());
        assertEquals(tip.getUUID(), tipCopy.getUUID());
    }
}

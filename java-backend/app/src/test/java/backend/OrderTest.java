package backend;

import backend.authentication.UserManager;
import backend.database.DatabaseConnector;
import backend.exceptions.*;
import backend.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderTest {

    private static final List<OrderLine> orderLines = new LinkedList<>();
    private static User user;
    private static final String FIRSTNAME = "asdasda",
            SURNAME = "asdads",
            EMAIL = "testuseer2@gmail.com",
            PHONE_NUMBER = "07955 555555",
            PASSWORD = "asdadas.";

    private final static DatabaseConnector conn = TestUtils.getDatabaseConnector();
    private static UserManager userManager;
    private static final String SECRET = "secret";
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static double TOTAL_PRICE = 0d;

    @BeforeAll static void initUser() throws NonceIssueException, RegistrationException, InvalidNonceException, NoSuchAlgorithmException {
        userManager = new UserManager(TestUtils.getDatabaseConnector(), "asjdahdoiashdiashdisu");
        final long nonce = userManager.getNonce();
        user = new User(UUID.randomUUID(), FIRSTNAME, SURNAME, EMAIL, PHONE_NUMBER, UserType.CUSTOMER);

        final String jwt = userManager.register(user, PASSWORD, nonce);
        assertTrue(userManager.isValid(jwt));
    }

    @BeforeAll static void initOrderLines() throws SQLException {
        TestUtils.getDatabaseConnector().runOnDatabase((c) -> {
            try {
                double total = 0d;
                final int QUANTITY = 5;
                final String REQUESTS = "Extra Cheese";

                for (MenuItem item : MenuItem.getMenuItems(TestUtils.getDatabaseConnector(), true)) {
                    total += item.getPrice() * QUANTITY;
                    if (item.isActive() && item.inStock(c)) {
                        orderLines.add(new OrderLine(item, QUANTITY, REQUESTS));
                    }
                }

                TOTAL_PRICE = total;
            } catch (Exception e) {
                e.printStackTrace();
                fail(e);
            }
        });
    }

    @Test void testConstructor() throws IllegalQuantityException {
        new Order(UUID.randomUUID(), UUID.randomUUID(),
                orderLines, OrderStatus.UNCONFIRMED,
                LocalDateTime.now(), LocalDateTime.now(), 69);
    }

    @Test void testInactiveMenuItemsCannotBeOrdered() throws Exception {
        TestUtils.getDatabaseConnector().runOnDatabase((c) -> {
            try {
                int QUANTITY = 5;
                final String REQUESTS = "Extra Cheese";
                boolean isInactive = false;
                List<OrderLine> orderLines = new LinkedList<>();

                for (MenuItem item : MenuItem.getMenuItems(TestUtils.getDatabaseConnector(), false)) {
                    orderLines.add(new OrderLine(item, QUANTITY, REQUESTS));
                    if (!item.isActive() || !item.inStock(c)) {
                        isInactive = true;
                    }
                }

                assertTrue(isInactive);

                final Order o = new Order(UUID.randomUUID(), UUID.randomUUID(),
                        orderLines, OrderStatus.UNCONFIRMED,
                        LocalDateTime.now(), LocalDateTime.now(), 69);

                assertThrows(InvalidOrderException.class, () -> {
                    o.addToDatabase(TestUtils.getDatabaseConnector());
                });
            } catch (Exception e) {
                e.printStackTrace();
                fail(e);
            }
        });
    }

    @Test void testGetters() throws IllegalQuantityException {
        final UUID orderid = UUID.randomUUID();
        final UUID owneruuid = UUID.randomUUID();
        final OrderStatus status = OrderStatus.UNCONFIRMED;
        final String iKey = "wee wee";
        final LocalDateTime made = LocalDateTime.now();
        final LocalDateTime updated = LocalDateTime.now().minusDays(180);
        final int tableno = 5;
        //final LocalDateTime prepTime = LocalDateTime.now().plusMinutes(5);
        final Order order = new Order(orderid, owneruuid, orderLines, status, made, updated, tableno);

        assertEquals(orderid, order.getUUID());
        assertEquals(owneruuid, order.getOwnerUUID());
        assertEquals(orderLines, order.getOrderLines());
        assertEquals(status, order.getOrderStatus());
        assertEquals(made, order.getPlacedTime());
        assertEquals(updated, order.getLastChangeTime());
        assertEquals(tableno, order.getTableno());
        assertEquals(4, order.getPrepTime());
    }

    @Test void testAsJson() {
        final UUID orderid = UUID.randomUUID();
        final UUID owneruuid = UUID.randomUUID();
        final OrderStatus status = OrderStatus.UNCONFIRMED;
        final LocalDateTime made = LocalDateTime.now();
        final LocalDateTime updated = LocalDateTime.now().minusDays(180);
        final int tableno = 5;

        final Order order = new Order(orderid, owneruuid, orderLines, status, made, updated, tableno);
        final JSONObject obj = order.asJson();

        assertEquals(owneruuid.toString(), obj.get("owner-uuid"));
        assertEquals(orderid.toString(), obj.get("uuid"));
        assertEquals(status.toString(), obj.get("status"));
        assertEquals(made.toString(), obj.get("placed-time"));
        assertEquals(updated.toString(), obj.get("last-changed-time"));
        assertEquals(tableno, obj.get("table-number"));
        assertEquals(order.getPrepTime(), obj.get("est-prep-time"));

        assertTrue(obj.get("order-lines") instanceof JSONArray);
        final JSONArray arr = (JSONArray) obj.get("order-lines");
        assert(arr.size() == orderLines.size());

        obj.toJSONString();
    }

    @Test void testDbWriteAndRead() throws Exception {
        final UUID owneruuid = user.getUUID();
        final OrderStatus status = OrderStatus.UNCONFIRMED;
        final LocalDateTime made = LocalDateTime.now();
        final LocalDateTime updated = LocalDateTime.now().minusDays(180);
        final int tableno = 5;

        final Order order = new Order(ORDER_ID, owneruuid, orderLines, status, made, updated, tableno);
        order.addToDatabase(TestUtils.getDatabaseConnector());

        final Order order2 = Order.fromUUID(ORDER_ID, TestUtils.getDatabaseConnector());
        if (TOTAL_PRICE < Order.AUTO_APPROVE_THRESHOLD) {
            assertEquals(order2.getOrderStatus(), OrderStatus.IN_PROGRESS);
        } else {
            assertEquals(order2.getOrderStatus(), OrderStatus.UNCONFIRMED);
        }

        System.err.println(order.asJson().toJSONString());
        System.err.println(order2.asJson().toJSONString());
        assertEquals(order, order2);
    }

    @Test void testStockUpdates() throws Exception {
        final UUID owneruuid = user.getUUID();
        final OrderStatus status = OrderStatus.UNCONFIRMED;
        final LocalDateTime made = LocalDateTime.now();
        final LocalDateTime updated = LocalDateTime.now().minusDays(180);
        final int tableno = 5;
        final int[] i = {0};
        List<OrderLine> orderLines = new LinkedList<>();

        for (MenuItem item : MenuItem.getMenuItems(TestUtils.getDatabaseConnector(), false)) {
            if (item.getName().equals("big chungus")) {
                orderLines.add(new OrderLine(item, 5, "E"));
            }
        }

        assertThrows(InvalidOrderException.class, ()-> {
            for (i[0] = 0; i[0] < 10; i[0]++) {
                final Order order = new Order(UUID.randomUUID(), owneruuid, orderLines, status, made, updated, tableno);
                order.addToDatabase(TestUtils.getDatabaseConnector());
            }
        });
    }

    @Test void testUpdateStatus() throws Exception {
        final Order order = Order.fromUUID(ORDER_ID, TestUtils.getDatabaseConnector());
        order.updateStatus(OrderStatus.CANCELLED, TestUtils.getDatabaseConnector());
    }

    @Test void testUpdateOrderLine() throws Exception {
        final Order order = Order.fromUUID(ORDER_ID, TestUtils.getDatabaseConnector());
        order.updateOrderLine(orderLines.get(0).getMenuItem().getUUID(), orderLines.get(0), TestUtils.getDatabaseConnector());
    }

    @Test void testCalculatePrepTime() {
        final UUID orderid = UUID.randomUUID();
        final UUID owneruuid = UUID.randomUUID();
        final OrderStatus status = OrderStatus.UNCONFIRMED;
        final String iKey = "wee wee";
        final LocalDateTime made = LocalDateTime.now();
        final LocalDateTime updated = LocalDateTime.now().minusDays(180);
        final int tableno = 5;
        final Order order = new Order(orderid, owneruuid, orderLines, status, made, updated, tableno);

        assertEquals(4, order.getPrepTime());
    }
  
    @AfterAll static void testGetActiveOrders() throws Exception {
        assertTrue(Order.getOrders(TestUtils.getDatabaseConnector(), true).size() >= 1);
    }

    @Test void getOrders() throws SQLException, InvalidOrderException {
        List<Order> orders = Order.getOrders(TestUtils.getDatabaseConnector(), false);

        for (int i = 0; i < orders.size(); i++) {
                assertNotNull(orders.get(i).getUUID());
                assertNotNull(orders.get(i).getOwnerUUID());
                assertNotNull(orders.get(i).getOrderLines());
                assertNotNull(orders.get(i).getOrderStatus());
                assertNotNull(orders.get(i).getPlacedTime());
                assertNotNull(orders.get(i).getLastChangeTime());
                assertNotNull(orders.get(i).getTableno());
        }
    }

    @Test void getOrdersFromCustId() throws SQLException {
        final UUID owneruuid = user.getUUID();
        List<Order> orders = Order.getOrdersFromCustId(TestUtils.getDatabaseConnector(), owneruuid, false);

        for (int i = 0; i < orders.size(); i++) {
            assertNotNull(orders.get(i).getUUID());
            assertTrue(orders.get(i).getOwnerUUID().equals(owneruuid));
            assertNotNull(orders.get(i).getOrderLines());
            assertNotNull(orders.get(i).getOrderStatus());
            assertNotNull(orders.get(i).getPlacedTime());
            assertNotNull(orders.get(i).getLastChangeTime());
            assertNotNull(orders.get(i).getTableno());
        }
    }
}

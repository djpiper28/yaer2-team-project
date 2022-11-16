/*
 * Test that servlets can be accessed the end points that are mean to be on.
 */
package backend;

import backend.authentication.UserManager;
import backend.database.DatabaseConnector;
import backend.exceptions.*;
import backend.model.*;
import backend.servlets.NotFoundServlet;
import backend.servlets.Router;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.lang.Exception;
import java.security.NoSuchAlgorithmException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;

import static org.junit.jupiter.api.Assertions.*;

public class ServletTest {
    private static volatile Thread routerThread;
    private static final String server_url = "127.0.0.1";
    private static final int server_port = 8009;
    private static User user;
    private static UserManager userManager;
    private static final String FIRSTNAME = "asdasda",
            SURNAME = "asdads",
            EMAIL = "testuser123@gmail.com",
            PHONE_NUMBER = "07955 535555",
            PASSWORD = "asdadas.";
    private static Order order = null;

    @BeforeAll
    static void initRouterTest() throws InterruptedException, SQLException, NonceIssueException, RegistrationException, InvalidNonceException, NoSuchAlgorithmException, InvalidOrderException {
        DatabaseConnector conn = TestUtils.getDatabaseConnector();
        routerThread = new Thread(() -> {
            try {
                Router.initRouter(server_url, server_port, conn, new UserManager(conn, "secret"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        routerThread.setDaemon(true);
        routerThread.start();

        // Sleep for 3 seconds to let jeetty set itself up
        Thread.sleep(3000);

        userManager = new UserManager(TestUtils.getDatabaseConnector(), "asjdahdoiashdiashdisu");
        final long nonce = userManager.getNonce();
        user = new User(UUID.randomUUID(), FIRSTNAME, SURNAME, EMAIL, PHONE_NUMBER, UserType.WAITER_STAFF);

        final String jwt = userManager.register(user, PASSWORD, nonce);
        assertTrue(userManager.isValid(jwt));

        final UUID ORDER_ID = UUID.randomUUID();
        final UUID custUuid = user.getUUID();
        final OrderStatus status = OrderStatus.UNCONFIRMED;
        final LocalDateTime made = LocalDateTime.now();
        final LocalDateTime updated = LocalDateTime.now().minusDays(180);
        final int tableno = 5;

        final List<OrderLine> orderLines = new LinkedList<>();
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

        order = new Order(ORDER_ID, custUuid, orderLines, status, made, updated, tableno);
        order.addToDatabase(TestUtils.getDatabaseConnector());
    }

    @Test
    void testNotFound() throws IOException {
        String url = String.format("http://%s:%d/", server_url, server_port);
        Document document = Jsoup.connect(url).ignoreContentType(true).get();
        System.err.println(document.text());
        assertEquals(document.text(), NotFoundServlet.INDEX_STATIC);

        url = String.format("http://%s:%d/ap/asdjasdhasuydgasuygy", server_url, server_port);
        document = Jsoup.connect(url).ignoreContentType(true).get();
        System.err.println(document.text());
        assertEquals(document.text(), NotFoundServlet.INDEX_STATIC);
    }

    @Test
    void testMenu() throws IOException, ParseException {
        final String url = String.format("http://%s:%d/api/menu", server_url, server_port);
        final Document document = Jsoup.connect(url).ignoreContentType(true).get();
        System.err.println(document.text());
        JSONObject root = (JSONObject) JSONValue.parseWithException(document.text());
        JSONArray types = (JSONArray) root.get("types");
        for (Object obj : types) {
            JSONObject item = (JSONObject) obj;

            // Assert not null
            assertNotNull(item.get("uuid"));
            assertNotNull(item.get("name"));
            assertNotNull(item.get("image-uri"));
            assertNotNull(item.get("desc"));

            // Assert that the types are correct
            assertTrue(item.get("uuid") instanceof String);
            assertTrue(item.get("name") instanceof String);
            assertTrue(item.get("image-uri") instanceof String);
            assertTrue(item.get("desc") instanceof String);

            // Assert that the uuid is a uuid
            String uuid = (String) item.get("uuid");
            assertNotNull(UUID.fromString(uuid));
        }

        JSONArray items = (JSONArray) root.get("items");
        for (Object obj : items) {
            JSONObject item = (JSONObject) obj;

            // Assert not null
            assertNotNull(item.get("uuid"));
            assertNotNull(item.get("name"));
            assertNotNull(item.get("description"));
            assertNotNull(item.get("image-uri"));
            assertNotNull(item.get("price"));
            assertNotNull(item.get("in-stock"));
            assertNotNull(item.get("item-type"));
            assertNotNull(item.get("added-at"));

            // Assert that the types are correct
            assertTrue(item.get("uuid") instanceof String);
            assertTrue(item.get("name") instanceof String);
            assertTrue(item.get("description") instanceof String);
            assertTrue(item.get("image-uri") instanceof String);
            assertTrue(item.get("price") instanceof Double);
            assertTrue(item.get("in-stock") instanceof Boolean);
            assertTrue(item.get("item-type") instanceof String);
            assertTrue(item.get("added-at") instanceof Long);

            // Assert that the uuid is a uuid
            String uuid = (String) item.get("uuid");
            assertNotNull(UUID.fromString(uuid));

            uuid = (String) item.get("item-type");
            assertNotNull(UUID.fromString(uuid));
        }
    }

    @Test
    void testGetNonce() throws Exception {
        final String url = String.format("http://%s:%d/api/getnonce", server_url, server_port);
        final Document document = Jsoup.connect(url).ignoreContentType(true).get();
        System.err.println(document.text());

        JSONObject object = (JSONObject) JSONValue.parse(document.text());
        assertNotNull(object.get("nonce"));
        String value = (String) object.get("nonce");
        long nonce = Long.valueOf(value);

        Router.router.getUserManager().isNonceValid(nonce);
    }

    /**
     * Tests a valid registration and login
     */
    @Test
    void testRegisterLogin() throws Exception {
        final Router r = Router.router;
        final String email = "nigel_farage@englandmail.eu";
        final String firstname = "nigel";
        final String surname = "farage";
        final String phoneNumber = "08000 001066";
        final String password = "password123123123";
        final User user = User.createNewCustomer(firstname, surname, email, phoneNumber);
        //ServletTest.user = user;

        // Register
        String url = String.format("http://%s:%d/api/register", server_url, server_port);
        final JSONObject obj = user.asJson();
        obj.put("nonce", String.valueOf(r.getUserManager().getNonce()));
        obj.put("password", PASSWORD);

        System.out.println(obj.toJSONString());
        Document document = Jsoup.connect(url).ignoreContentType(true).requestBody(obj.toJSONString()).post();
        System.err.println(document.text());

        final JSONObject robject = (JSONObject) JSONValue.parse(document.text());
        final String refresh = (String) robject.get("refresh-token");
        long nonce = r.getUserManager().getNonce();
        assertTrue(r.getUserManager().isValid(r.getUserManager().useRefresh(refresh, nonce)));

        // Login with this account
        url = String.format("http://%s:%d/api/login", server_url, server_port);

        final JSONObject loginobject = new JSONObject();
        loginobject.put("nonce", String.valueOf(r.getUserManager().getNonce()));
        loginobject.put("email", email);
        loginobject.put("password", PASSWORD);

        document = Jsoup.connect(url).ignoreContentType(true).requestBody(loginobject.toJSONString()).post();

        final JSONObject lobject = (JSONObject) JSONValue.parse(document.text());
        assertEquals((String) lobject.get("user-type"), UserType.CUSTOMER.toString());

        final String refresh2 = (String) lobject.get("refresh-token");
        nonce = r.getUserManager().getNonce();
        assertTrue(r.getUserManager().isValid(r.getUserManager().useRefresh(refresh2, nonce)));

        // Test using refresh token
        url = String.format("http://%s:%d/api/refresh", server_url, server_port);

        nonce = r.getUserManager().getNonce();
        final JSONObject robj = new JSONObject();
        robj.put("nonce", String.valueOf(nonce));
        robj.put("refresh-token", refresh2);

        document = Jsoup.connect(url).ignoreContentType(true).requestBody(robj.toJSONString()).post();
        final JSONObject refreshobject = (JSONObject) JSONValue.parse(document.text());
        final String access = (String) refreshobject.get("access-token");
        nonce = r.getUserManager().getNonce();
        assertTrue(r.getUserManager().isValid(access));
        assertTrue(r.getUserManager().isNonceValid(nonce));
    }

    private static String getAccessToken() throws Exception {
        final UserManager u = Router.router.getUserManager();
        long nonce = u.getNonce();
        final String refresh = u.login(ServletTest.user.getEmail(), PASSWORD, nonce);
        nonce = u.getNonce();
        return u.useRefresh(refresh, nonce);
    }

    @Test
    void testUpdateStatus() throws SQLException {
        try {
            final long nonce = Router.router.getUserManager().getNonce();
            final JSONObject obj = new JSONObject();

            obj.put("order-id", order.getUUID().toString());
            obj.put("new-status", 3);
            obj.put("nonce", String.valueOf(nonce));
            obj.put("access-token", getAccessToken());

            final String url = String.format("http://%s:%d/api/orderstatus", server_url, server_port);
            final Document document = Jsoup.connect(url).ignoreContentType(true).requestBody(obj.toJSONString()).post();
            final JSONObject retObj = (JSONObject) JSONValue.parse(document.text());
            assertTrue(retObj.get("success") instanceof Boolean);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e);
        }
    }

    @Test
    void testNotificationServlet() throws Exception {
        long nonce = Router.router.getUserManager().getNonce();
        JSONObject obj = new JSONObject();
        final long tableNo = 5;
        final String notifBody = "test";

        obj.put("nonce", String.valueOf(nonce));
        obj.put("access-token", getAccessToken());
        obj.put("table-no", tableNo);
        obj.put("notif-body", notifBody);

        String url = String.format("http://%s:%d/api/notify", server_url, server_port);
        Document document = Jsoup.connect(url).ignoreContentType(true).requestBody(obj.toJSONString()).post();
        JSONObject retObj = (JSONObject) JSONValue.parse(document.text());
        assertTrue(retObj.get("success") instanceof Boolean);

        AtomicReference<String> uuid = new AtomicReference<>("fail");
        TestUtils.getDatabaseConnector().runOnDatabase(conn -> {
            try {
                CallableStatement statement = conn.prepareCall("select notif_id from notifications;");
                ResultSet rs = statement.executeQuery();
                rs.next();
                uuid.set(rs.getString(1));
            } catch (SQLException e) {
                e.printStackTrace();
                fail();
            }
        });

        obj = new JSONObject();
        nonce = Router.router.getUserManager().getNonce();
        obj.put("notif-uuid", uuid.get());
        obj.put("nonce", String.valueOf(nonce));
        obj.put("access-token", getAccessToken());

        url = String.format("http://%s:%d/api/rm-notif", server_url, server_port);
        document = Jsoup.connect(url).ignoreContentType(true).requestBody(obj.toJSONString()).post();
        retObj = (JSONObject) JSONValue.parse(document.text());
        assertTrue(retObj.get("success") instanceof Boolean);
    }

    @Test void testUpdateTableNo() throws SQLException {
        TestUtils.getDatabaseConnector().runOnDatabase((c) -> {
            try {
                final long nonce = Router.router.getUserManager().getNonce();
                final JSONObject obj = new JSONObject();

                obj.put("order-id", order.getUUID().toString());
                obj.put("new-number", 5);;
                obj.put("nonce", String.valueOf(nonce));
                obj.put("access-token", getAccessToken());

                final String url = String.format("http://%s:%d/api/modifytableno", server_url, server_port);
                final Document document = Jsoup.connect(url).ignoreContentType(true).requestBody(obj.toJSONString()).post();
                final JSONObject retObj = (JSONObject) JSONValue.parse(document.text());
                assertTrue(retObj.get("success") instanceof Boolean);

            } catch (Exception e) {
                e.printStackTrace();
                fail(e);
            }
        });
    }

    @Test void testUpdateOrder() throws SQLException {
        TestUtils.getDatabaseConnector().runOnDatabase((c) -> {
            try {
                final long nonce = Router.router.getUserManager().getNonce();
                final JSONObject obj = new JSONObject();
                final JSONArray orderlines = new JSONArray();
                MenuItem menuItem = MenuItem.getMenuItems(TestUtils.getDatabaseConnector(), true).get(0);
                OrderLine oldOrderLine = new OrderLine(menuItem, 2, "cheese please");
                OrderLine newOrderLine = new OrderLine(menuItem, 2, "this time, no cheese please");
                orderlines.add(oldOrderLine.asJson());
                orderlines.add(newOrderLine.asJson());

                obj.put("order-id", order.getUUID().toString());
                obj.put("menu-id", menuItem.getUUID().toString());
                obj.put("order-lines", orderlines);
                obj.put("nonce", String.valueOf(nonce));
                obj.put("access-token", getAccessToken());

                final String url = String.format("http://%s:%d/api/modifyorderitem", server_url, server_port);
                final Document document = Jsoup.connect(url).ignoreContentType(true).requestBody(obj.toJSONString()).post();
                final JSONObject retObj = (JSONObject) JSONValue.parse(document.text());
                assertTrue(retObj.get("success") instanceof Boolean);

            } catch (Exception e) {
                e.printStackTrace();
                fail(e);
            }
        });
    }

    @AfterAll
    static void testOrderAndTip() throws Exception {
        //adding order
        TestUtils.getDatabaseConnector().runOnDatabase((c) -> {
            try {
                final long nonce = Router.router.getUserManager().getNonce();
                final List<MenuItem> items = MenuItem.getMenuItems(TestUtils.getDatabaseConnector(), true);
                final JSONObject obj = new JSONObject();

                obj.put("access-token", getAccessToken());
                obj.put("table-number", 5);
                obj.put("nonce", String.valueOf(nonce));

                final JSONArray arr = new JSONArray();
                for (MenuItem item : items) {
                    if (item.inStock(c) && item.isActive() && !item.getName().equals("big chungus")) {
                        final JSONObject itemObj = new JSONObject();
                        itemObj.put("quantity", 5);
                        itemObj.put("special-requests", "cheese");
                        itemObj.put("menu-id", item.getUUID().toString());

                        arr.add(itemObj);
                    }
                }

                obj.put("items", arr);

                final String url = String.format("http://%s:%d/api/order", server_url, server_port);
                final Document document = Jsoup.connect(url).ignoreContentType(true).requestBody(obj.toJSONString()).post();

                final JSONObject ret = (JSONObject) JSONValue.parse(document.text());
                assertTrue(ret.get("order-id") instanceof String);
            } catch (Exception e) {
                e.printStackTrace();
                fail(e);
            }
        });

        //adding tip
        try {
            final long nonce = Router.router.getUserManager().getNonce();
            final JSONObject obj = new JSONObject();

            obj.put("order-id", order.getUUID().toString());
            obj.put("amount", 6.5);
            obj.put("nonce", String.valueOf(nonce));
            obj.put("access-token", getAccessToken());

            final String url = String.format("http://%s:%d/api/tips", server_url, server_port);
            final Document document = Jsoup.connect(url).ignoreContentType(true).requestBody(obj.toJSONString()).post();
            final JSONObject retObj = (JSONObject) JSONValue.parse(document.text());
            assertTrue(retObj.get("success") instanceof Boolean);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e);
        }
    }

    private static final int PERF_GOAL = 100;

    /*
     * The menu should be able to serve 100 requests in about 3 seconds
     */
    @Test
    void performanceTest() throws InterruptedException {
        final long start = System.currentTimeMillis();

        final int t = Runtime.getRuntime().availableProcessors() * 5;
        final Thread[] threads = new Thread[t];
        final int reqsPerThread = PERF_GOAL / t;
        System.out.printf("Requests per thread %d.\n", reqsPerThread);

        for (int i = 0; i < t; i++) {
            final int ii = i;
            threads[i] = new Thread(() -> {
                final String url = String.format("http://%s:%d/api/menu", server_url, server_port);
                for (int j = 0; j < reqsPerThread; j++) {
                    try {
                        Jsoup.connect(url).ignoreContentType(true).get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                System.out.printf("Thread %d finished.\n", ii);
            }, "Performance test thread");
            threads[i].start();
        }

        for (int i = 0; i < t; i++) {
            threads[i].join();
        }

        final long end = System.currentTimeMillis();
        System.out.printf("Time taken %d finished.\n", end - start);
        assertTrue(end - start <= 3000);
    }

    @AfterAll
    static void stopRouter() {
        routerThread.interrupt();
    }

}

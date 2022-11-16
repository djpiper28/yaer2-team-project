/*
 * Test that the menu item does what it says on the tin.
 */
package backend;

import backend.model.MenuItem;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.runner.Description;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MenuItemTest {
    @Test void testConstructorAndGetters() {
        final UUID uuid = UUID.randomUUID();
        final UUID typeid = UUID.randomUUID();
        final String NAME = "NAME", DESCRIPTION = "DESC", IMAGE_URI = "IMAGE_URI";
        final double PRICE = 6.32d;
        final int PREP_TIME = 3;
        final boolean ACTIVE = true;
        final boolean INSTOCK = true;
        MenuItem item = new backend.model.MenuItem(uuid, NAME, DESCRIPTION, IMAGE_URI, PRICE, PREP_TIME, ACTIVE, INSTOCK, typeid, LocalDateTime.now());

        assertEquals(uuid, item.getUUID());
        assertEquals(NAME, item.getName());
        assertEquals(DESCRIPTION, item.getDescription());
        assertEquals(IMAGE_URI, item.getImageURI());
        assertEquals(PRICE, item.getPrice());
        assertEquals(PREP_TIME, item.getPrepTime());
        assertEquals(ACTIVE, item.isActive());
        assertEquals(INSTOCK, item.wasInStock());
        assertEquals(typeid, item.getTypeUUID());
        assertNotNull(item.getAddedAt());
    }

    @Test void testEquals() {
        final UUID uuid = UUID.randomUUID();
        final String NAME = "NAME", DESCRIPTION = "DESC", IMAGE_URI = "IMAGE_URI";
        final double PRICE = 6.32d;
        final int PREP_TIME = 3;
        final boolean ACTIVE = true;
        final boolean INSTOCK = true;
        final UUID typeid = UUID.randomUUID();
        final LocalDateTime t = LocalDateTime.now();

        MenuItem item = new MenuItem(uuid, NAME, DESCRIPTION, IMAGE_URI, PRICE, PREP_TIME, ACTIVE, INSTOCK, typeid, t);

        assertEquals(item, item);
        assertEquals(item, new MenuItem(uuid, NAME, DESCRIPTION, IMAGE_URI, PRICE, PREP_TIME, ACTIVE, INSTOCK, typeid, t));
        assertNotEquals(item, "wrong class");
        assertNotEquals(item, null);
    }

    // Hash codes are based off of UUID only
    @Test void testHashCodes() {
        final UUID uuid = UUID.randomUUID();
        final String NAME = "NAME", DESCRIPTION = "DESC", IMAGE_URI = "IMAGE_URI";
        final double PRICE = 6.32d;
        final int PREP_TIME = 3;
        final boolean ACTIVE = true;
        final boolean INSTOCK = true;
        final UUID typeid = UUID.randomUUID();

        MenuItem item = new MenuItem(uuid, NAME, DESCRIPTION, IMAGE_URI, PRICE, PREP_TIME, ACTIVE, INSTOCK, typeid, LocalDateTime.now());
        MenuItem item2 = new MenuItem(uuid, NAME, DESCRIPTION, IMAGE_URI, PRICE, PREP_TIME, ACTIVE, INSTOCK, typeid, LocalDateTime.now());
        MenuItem item3 = new MenuItem(uuid, NAME, DESCRIPTION, IMAGE_URI, PRICE + 6d, PREP_TIME, ACTIVE, INSTOCK, typeid, LocalDateTime.now());

        assertEquals(item.hashCode(), item2.hashCode());
        assertEquals(item.hashCode(), item3.hashCode());
    }

    @Test void testAsJson() {
        final UUID uuid = UUID.randomUUID();
        final String NAME = "NAME", DESCRIPTION = "DESC", IMAGE_URI = "IMAGE_URI";
        final double PRICE = 6.32d;
        final int PREP_TIME = 3;
        final boolean ACTIVE = false;
        final boolean INSTOCK = true;
        final UUID typeid = UUID.randomUUID();
        final LocalDateTime t = LocalDateTime.now();

        MenuItem menuItem = new backend.model.MenuItem(uuid, NAME, DESCRIPTION, IMAGE_URI, PRICE, PREP_TIME, ACTIVE, INSTOCK, typeid, t);
        JSONObject item = menuItem.asJson();

// Assert that the uuid is a uuid
        String uuid_in = (String) item.get("uuid");

        assertEquals(UUID.fromString(uuid_in), uuid);
        assertEquals((String) item.get("name"), NAME);
        assertEquals((String) item.get("description"), DESCRIPTION);
        assertEquals((String) item.get("image-uri"), IMAGE_URI);
        assertEquals((Double) item.get("price"), PRICE);
        assertEquals((boolean) item.get("active"), ACTIVE);
        assertEquals((boolean) item.get("in-stock"), INSTOCK);
        assertEquals((long) item.get("added-at"), t.toEpochSecond(ZoneOffset.UTC));
        String typeid_in = (String) item.get("item-type");
        assertEquals((int) item.get("prep-time"), PREP_TIME);

        assertEquals(UUID.fromString(typeid_in), typeid);
    }

    @Test void testGetMenuItems() throws Exception {
        for (MenuItem item : MenuItem.getMenuItems(TestUtils.getDatabaseConnector(), true)) {
            assertEquals(item, MenuItem.fromUUID(item.getUUID(), TestUtils.getDatabaseConnector()));
        }
    }

    @Test void testInStockBaseCase() throws Exception {
        TestUtils.getDatabaseConnector().runOnDatabase((c) -> {
            try {
                for (MenuItem item : MenuItem.getMenuItems(TestUtils.getDatabaseConnector(), true)) {
                    final boolean expected = !item.getName().equals("not in stock");
                    System.err.println(item.getName());
                    assertEquals(item.inStock(c), expected);
                }
            } catch(Exception e) {
                fail(e);
            }
        });
    }

    private static final int PERF_GOAL = 100;
    /*
     * The menu should be able to serve 100 requests in about 3 seconds
     */
    @Test void performanceTest() throws InterruptedException {
        final long start = System.currentTimeMillis();

        final int t = 100;
        final Thread[] threads = new Thread[t];
        final int reqsPerThread = PERF_GOAL / t;
        System.out.printf("Requests per thread %d.\n", reqsPerThread);

        for (int i = 0; i < t; i++) {
            final int ii = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < reqsPerThread; j++) {
                    try {
                        MenuItem.getMenuItems(TestUtils.getDatabaseConnector(), true);
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
}

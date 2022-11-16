package backend;

import backend.exceptions.IllegalQuantityException;
import backend.model.MenuItem;
import backend.model.OrderLine;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderLineTest {
    private static final MenuItem ITEM = new MenuItem(UUID.randomUUID(), "Name", "Desc", "ImageURI", 5d, 3, true, true, UUID.randomUUID(), LocalDateTime.now());
    private static final int QUANTITY = 5;
    private static final String REQUESTS = "Extra Cheese";
    
    @Test void testConstructor() throws IllegalQuantityException {
        new OrderLine(ITEM, QUANTITY, REQUESTS);
    }

    @Test void testGetters() throws IllegalQuantityException {
        final OrderLine testOrderLine = new OrderLine(ITEM, QUANTITY, REQUESTS);
        assertEquals(testOrderLine.getMenuItem(), ITEM);
        assertEquals(testOrderLine.getQuantity(), QUANTITY);
        assertEquals(testOrderLine.getSpecialRequest(), REQUESTS);
    }

    @Test void testAsJson() {
        final OrderLine testOrderLine = new OrderLine(ITEM, QUANTITY, REQUESTS);
        final JSONObject jsonOrderLine = testOrderLine.asJson();

        // Values
        assertEquals(jsonOrderLine.get("menu-item"), ITEM.asJson());
        assertEquals(jsonOrderLine.get("quantity"), QUANTITY);
        assertEquals(jsonOrderLine.get("special-request"), REQUESTS);

        jsonOrderLine.toJSONString();
    }
}

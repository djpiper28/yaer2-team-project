package backend;

import backend.model.MenuItem;
import backend.model.MenuType;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MenuTypeTest {

    @Test void testConstructorAndGetters() {
        final UUID uuid = UUID.randomUUID();
        final String name = "name";
        final String imageUri = "imageUri";
        final String desc = "desc";

        MenuType type = new MenuType(uuid, name, imageUri, desc);
        assertEquals(type.getUUID(), uuid);
        assertEquals(type.getName(), name);
        assertEquals(type.getImageUri(), imageUri);
        assertEquals(type.getDesc(), desc);
    }

    @Test void testGetAllTypes() throws Exception {
        List<MenuType> types = MenuType.getMenuTypes(TestUtils.getDatabaseConnector());
        assertTrue(types.size() > 0);
        types.forEach(Assertions::assertNotNull);
    }

    @Test void testAsJson() {
        final UUID uuid = UUID.randomUUID();
        final String name = "name";
        final String imageUri = "imageUri";
        final String desc = "desc";

        MenuType type = new MenuType(uuid, name, imageUri, desc);

        JSONObject ret = new JSONObject();
        ret.put("uuid", uuid.toString());
        ret.put("name", name);
        ret.put("image-uri", imageUri);
        ret.put("desc", desc);

        assertEquals(ret, type.asJson());
    }

}

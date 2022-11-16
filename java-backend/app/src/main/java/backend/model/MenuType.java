package backend.model;

import backend.database.DatabaseConnector;
import org.json.simple.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A model for the menu types.
 *
 * @author Danny
 * @version 2
 */
public class MenuType {

    private final UUID typeId;
    private final String name;
    private final String imageUri;
    private final String desc;

    /**
     * Constructor for the read only menu item.
     *
     * @param typeId      the type id (uuid)
     * @param name        the type name as shown on the site
     * @param imageUri    the image url as shown on the site
     * @param description the description of the category
     * @since 2
     */
    public MenuType(UUID typeId, String name, String imageUri, String desc) {
        this.typeId = typeId;
        this.name = name;
        this.imageUri = imageUri;
        this.desc = desc;
    }

    /**
     * Gets the menu types.
     *
     * @param connector the connector to the database
     * @return a list of all of the menu types
     * @throws SQLException thrown when an error occurs whilst getting the types
     * @since 1
     */
    public static List<MenuType> getMenuTypes(DatabaseConnector connector) throws SQLException {
        List<MenuType> ret = new LinkedList<>();
        final SQLException[] e = {null};

        connector.runOnDatabase((conn -> {
            try {
                PreparedStatement ps = conn.prepareStatement("select typeid, item_type, item_image, item_desc from menutypes;");
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String id = rs.getString(1);
                    String name = rs.getString(2);
                    String image_uri = rs.getString(3);
                    String desc = rs.getString(4);

                    MenuType i = new MenuType(UUID.fromString(id), name, image_uri, desc);
                    ret.add(i);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                e[0] = ex;
            }
        }));

        if (e[0] != null) {
            throw e[0];
        }

        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuType menuType = (MenuType) o;
        return Objects.equals(typeId, menuType.typeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeId);
    }

    /**
     * Gets the UUID
     *
     * @return the UUID of the type
     * @since 1
     */
    public UUID getUUID() {
        return typeId;
    }

    /**
     * Gets the name
     *
     * @return the name of the type
     * @since 1
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the image uri
     *
     * @return the image uri of the type
     * @since 1
     */
    public String getImageUri() {
        return imageUri;
    }

    /**
     * Gets the description of the type
     *
     * @return the desc
     * @since 2
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Return the menu type as json
     *
     * @return a json object of the menu type
     * @since 1
     */
    public JSONObject asJson() {
        JSONObject ret = new JSONObject();
        ret.put("uuid", this.typeId.toString());
        ret.put("name", this.name);
        ret.put("image-uri", this.imageUri);
        ret.put("desc", this.desc);

        return ret;
    }

}

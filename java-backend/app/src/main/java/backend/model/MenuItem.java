/*
 * Represents an item on a menu
 */
package backend.model;

import backend.database.DatabaseConnector;
import org.json.simple.JSONObject;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * An item on the menu and the data that represents it. This class is final to prohibit
 * any and all inheritence and, has no setters to force it to be immutable.
 *
 * @author Danny
 * @version 9
 * @see Order
 */
public final class MenuItem {
    private final UUID uuid, typeId;
    private final String name, description, imageURI;
    private final double price;
    private final int prep_time;
    private final boolean active;
    private final boolean inStock;
    private final LocalDateTime addedAt;

    /**
     * Inits a menu item from the given data.
     *
     * @param uuid        the uuid of the menu item
     * @param name        the name of the menu item
     * @param description the description of the menu item
     * @param imageURI    a relative uri to the menu item's image, such as "/cdn/burger1.png"
     * @param price       the price (in pounds) of the menu item
     * @param prepTime    the time to prepare an item
     * @param active      whether the order is active
     * @param inStock     whether the item is in stock
     * @param typeId      the type uuid
     * @param addedAt     the time it was added to the menu
     * @since 8
     */
    public MenuItem(UUID uuid, String name, String description, String imageURI, double price, int prepTime, boolean active, boolean inStock, UUID typeId, LocalDateTime addedAt) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.imageURI = imageURI;
        this.price = price;
        this.prep_time = prepTime;
        this.active = active;
        this.inStock = inStock;
        this.typeId = typeId;
        this.addedAt = addedAt;
    }

    /**
     * Gets a menu item from a uuid
     *
     * @param menuId    the menu item's uuid
     * @param connector the database connector to get the menu item from
     * @return the menu item from the uuid
     * @throws SQLException thrown when there is an error fetching the item
     * @since 3
     */
    public static MenuItem fromUUID(UUID menuId, DatabaseConnector connector) throws SQLException {
        final SQLException[] e = {null};
        MenuItem[] out = {null};

        connector.runOnDatabase((c) -> {
            try {
                PreparedStatement ps = c.prepareStatement("select name, description, imageuri, price, prep_time, active, typeid, added_at " +
                        "from menuitems where menuid = ?;");
                ps.setObject(1, menuId);

                ResultSet rs = ps.executeQuery();
                rs.next();

                String name, desc, imageuri;
                double price;
                int prep_time;
                boolean active;

                name = rs.getString(1);
                desc = rs.getString(2);
                imageuri = rs.getString(3);
                price = Double.parseDouble(rs.getString(4).substring(1));
                prep_time = rs.getInt(5);
                active = rs.getBoolean(6);
                final UUID type = UUID.fromString(rs.getString(7));
                final LocalDateTime addedAt = ((Timestamp) rs.getObject(8)).toLocalDateTime();

                MenuItem item = new MenuItem(menuId, name, desc, imageuri, price, prep_time, active, inStock(c, menuId), type, addedAt);

                out[0] = item;
            } catch (SQLException ex) {
                e[0] = ex;
                ex.printStackTrace();
            }
        });

        if (e[0] != null) {
            throw e[0];
        }

        if (out[0] == null) {
            throw new NullPointerException("Menu item is null");
        }

        return out[0];
    }

    /**
     * A method to get a menu item from a uuid which can be used within a transaction to stop race conditions,
     *
     * @param menuId the uuid of the item
     * @param conn   the connector to execute on
     * @return the menu item if found
     * @throws SQLException thrown when an error occurs getting the output
     */
    public static MenuItem fromUUID(UUID menuId, Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("select name, description, imageuri, price, prep_time, active, added_at " +
                "from menuitems where menuid = ?;");
        ps.setObject(1, menuId);

        ResultSet rs = ps.executeQuery();
        rs.next();

        String uuidTmp, name, desc, imageuri;
        double price;
        int prep_time;
        boolean active;

        name = rs.getString(1);
        desc = rs.getString(2);
        imageuri = rs.getString(3);
        price = Double.parseDouble(rs.getString(4).substring(1));
        prep_time = rs.getInt(5);
        active = rs.getBoolean(6);
        final UUID type = UUID.fromString(rs.getString(7));
        final LocalDateTime addedAt = (LocalDateTime) rs.getObject(8);

        return new MenuItem(menuId, name, desc, imageuri, price, prep_time, active, inStock(conn, menuId), type, addedAt);

    }

    /**
     * Queries the database for the menu and returns what it finds.
     *
     * @param conn           the database connector that stores the information
     * @param onlyShowActive whether to only show active items
     * @return a list of the items in the menu
     * @throws SQLException thrown when there was an error getting the data from the database
     * @since 5
     */
    public static List<MenuItem> getMenuItems(DatabaseConnector conn, boolean onlyShowActive) throws SQLException {
        final SQLException[] e = {null};
        List<MenuItem> out = new ArrayList<>();

        conn.runOnDatabase((c) -> {
            try {
                String activeClause = onlyShowActive ? "where active = true" : "";
                c.setAutoCommit(false);
                CallableStatement ps = c.prepareCall("select menuid, name, description, imageuri, price, prep_time, active, typeid, added_at " +
                        "from menuitems " + activeClause + ";");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String uuidTmp, name, desc, imageuri;
                    double price;
                    int prep_time;
                    boolean active;

                    uuidTmp = rs.getString(1);
                    name = rs.getString(2);
                    desc = rs.getString(3);
                    imageuri = rs.getString(4);
                    price = Double.parseDouble(rs.getString(5).substring(1));
                    prep_time = rs.getInt(6);
                    active = rs.getBoolean(7);
                    UUID typeid = UUID.fromString(rs.getString(8));
                    UUID uuid = UUID.fromString(uuidTmp);
                    final LocalDateTime addedAt = ((Timestamp) rs.getObject(9)).toLocalDateTime();

                    MenuItem item = new MenuItem(uuid, name, desc, imageuri, price, prep_time, active, inStock(c, uuid), typeid, addedAt);

                    out.add(item);
                }
                c.commit();
            } catch (SQLException ex) {
                e[0] = ex;
            }
        });

        if (e[0] != null) {
            throw e[0];
        }

        return out;
    }

    /**
     * A static helper method to see if an item is in stock, used by the constructor + the inStock method,
     *
     * @param connection
     * @param uuid
     * @return
     * @throws SQLException
     */
    private static boolean inStock(Connection connection, UUID uuid) throws SQLException {
        final PreparedStatement ps = connection.prepareStatement(
                "select inventoryitems.amount, inventorymenu.unitsrequired " +
                        "from inventoryitems " +
                        "inner join inventorymenu on inventoryitems.invid = inventorymenu.invid " +
                        "where inventorymenu.menuid = ?;");
        ps.setObject(1, uuid);
        final ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            // amount < needed
            if (rs.getInt(1) < rs.getInt(2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the menu items UUID
     *
     * @return the UUID of the menu item
     * @since 1
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * Returns the menu type UUID
     *
     * @return the type UUID of the menu item
     * @return the UUID of the menu type
     * @since 7
     */
    public UUID getTypeUUID() {
        return this.typeId;
    }

    /**
     * Gets the name of a menu item.
     *
     * @return the name of the menu item
     * @since 1
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the description of the menu item.
     *
     * @return the description of the menu item
     * @since 1
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the relative image URI of the menu item, note this is relative and will be
     * something such as "/cdn/burger1.png".
     *
     * @return the image uri
     * @since 1
     */
    public String getImageURI() {
        return this.imageURI;
    }

    /**
     * Gets the price that the menu item has.
     *
     * @return the price of the item
     * @since 1
     */
    public double getPrice() {
        return this.price;
    }

    /**
     * Gets the estimated prep time of an item
     *
     * @return
     * @since 4
     */
    public int getPrepTime() {
        return this.prep_time;
    }

    /**
     * Whether the order is active.
     *
     * @return whether the menu item is active
     * @since 5
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * The time the menu item was added to the menu.
     *
     * @return the datetime
     * @since 8
     */
    public LocalDateTime getAddedAt() {
        return this.addedAt;
    }

    /**
     * Return a menu item as a json object.
     *
     * @return the json of the menu item
     * @since 8
     */
    public JSONObject asJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("uuid", this.uuid.toString());
        jsonObject.put("name", this.name);
        jsonObject.put("description", this.description);
        jsonObject.put("image-uri", this.imageURI);
        jsonObject.put("price", this.price);
        jsonObject.put("active", this.active);
        jsonObject.put("in-stock", this.inStock);
        jsonObject.put("item-type", this.typeId.toString());
        jsonObject.put("prep-time", this.prep_time);
        jsonObject.put("added-at", this.addedAt.toEpochSecond(ZoneOffset.UTC)); // i promise this will cause no bugs

        return jsonObject;
    }

    /**
     * Gets whether a menu item is in stock, it uses a connector so that it can be run within a transaction,
     *
     * @param connection the connection of the transaction
     * @return whether the item is in stock or not
     * @throws SQLException thrown when an error occurs getting the data
     */
    public boolean inStock(Connection connection) throws SQLException {
        return inStock(connection, this.getUUID());
    }

    /**
     * Returns whether this was in stock when it was made.
     *
     * @return whether it was in stock at construction
     * @since 6
     */
    public boolean wasInStock() {
        return this.inStock;
    }

    /**
     * A helper to method to check that you can order N of these items
     *
     * @param connection database connector
     * @param n          the amount of this item you want to order
     * @return whether you can order n items
     * @throws SQLException
     * @since 9
     */
    public boolean canOrderN(Connection connection, int n) throws SQLException {
        final PreparedStatement ps = connection.prepareStatement(
                "select inventoryitems.amount, inventorymenu.unitsrequired " +
                        "from inventoryitems " +
                        "inner join inventorymenu on inventoryitems.invid = inventorymenu.invid " +
                        "where inventorymenu.menuid = ?;");
        ps.setObject(1, this.uuid);
        final ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            // amount < needed
            if (rs.getInt(1) < rs.getInt(2) * n) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return Objects.equals(uuid, menuItem.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}

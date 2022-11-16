/*
 * Represents an order that has been placed by a customer and its state.
 */
package backend.model;

import backend.database.DatabaseConnector;
import backend.exceptions.InvalidOrderException;
import backend.exceptions.OrderLineNotFoundException;
import backend.exceptions.OrderStatusNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An order, a set of order lines and, a collection of MenuItems.
 *
 * @author Danny, Flynn
 * @version 6
 * @see OrderLine
 */
public final class Order {
    public static final double AUTO_APPROVE_THRESHOLD = 50d;
    /**
     * Setup a logger
     *
     * @since 3
     */
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Order.class.getName());
    final private UUID uuid, owneruuid;
    final private List<OrderLine> orderLines;
    private int tableno;
    final private LocalDateTime placedTime;
    /**
     * The amount of seconds it will take to prepare
     *
     * @since 6
     */
    final private long prepTime;
    private OrderStatus status;
    /**
     * The last time the order was changed,
     *
     * @since 1
     */
    private LocalDateTime lastChangedTime;

    /**
     * Inits an order object from given data. It is not checked for validity.
     *
     * @param uuid            the UUID of the order (generated or, from the database)
     * @param owneruuid       the UUID of the customer who owns the order
     * @param orderLines      a list of the order lines which make this order up
     * @param status          the status of the order
     * @param placedTime      the time the order was placed
     * @param lastChangedTime the last time that the order was changed
     * @param tableno         the table number of the order
     * @since 2
     */
    public Order(UUID uuid, UUID owneruuid, List<OrderLine> orderLines, OrderStatus status,
                 LocalDateTime placedTime, LocalDateTime lastChangedTime,
                 int tableno) {
        this.owneruuid = owneruuid;
        this.uuid = uuid;
        this.orderLines = orderLines;
        this.status = status;
        this.placedTime = placedTime;
        this.lastChangedTime = lastChangedTime;
        this.tableno = tableno;
        this.prepTime = this.calculatePrepTime();
    }

    /**
     * Gets an order from a given UUID.
     *
     * @param uuid      order UUID
     * @param connector the database connecctor
     * @return the order object from the UUID
     * @throws SQLException thrown if it fails to find or has a connection issue
     * @since 4
     */
    public static Order fromUUID(UUID uuid, DatabaseConnector connector) throws SQLException {
        final SQLException[] ex = {null};
        final Order[] ret = {null};

        connector.runOnDatabase(conn -> {
            try {
                PreparedStatement ps = conn.prepareStatement("select customerid, tableno, status, ordertime, lastchangetime from orders where orderid = ?;");
                ps.setObject(1, uuid);

                ResultSet rs = ps.executeQuery();
                rs.next();

                final UUID ownerid = UUID.fromString(rs.getString(1));
                final int tableno = rs.getInt(2);
                final int status = rs.getInt(3);
                final OrderStatus orderStatus = OrderStatus.from(status);
                final LocalDateTime placedTime = rs.getTimestamp(4).toLocalDateTime();
                final LocalDateTime lastChanged = rs.getTimestamp(5).toLocalDateTime();
                final List<OrderLine> lines = new LinkedList<>();

                PreparedStatement getLines = conn.prepareStatement("select menuid, quantity, requests from orderlines where orderid = ?");
                getLines.setObject(1, uuid);

                ResultSet lineSet = getLines.executeQuery();
                while (lineSet.next()) {
                    final String menuId = lineSet.getString(1);
                    final int quantity = lineSet.getInt(2);
                    final String requests = lineSet.getString(3);

                    final MenuItem item = MenuItem.fromUUID(UUID.fromString(menuId), connector);
                    lines.add(new OrderLine(item, quantity, requests));
                }

                ret[0] = new Order(uuid, ownerid, lines, orderStatus, placedTime, lastChanged, tableno);
            } catch (SQLException e) {
                logger.error(e);
                ex[0] = e;
            } catch (OrderStatusNotFoundException e) {
                logger.error(e);
                ex[0] = new SQLException("Order status is invalid");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if (ex[0] != null) {
            throw ex[0];
        }

        if (ret[0] == null) {
            throw new SQLException("Order Not Found");
        }

        return ret[0];
    }

    /**
     * Queries the Order table in database and returns records based on conditions.
     *
     * @param dbConn         the database connector that stores the information
     * @param onlyShowActive whether to only show active items
     * @return a list of the orders
     * @throws SQLException thrown when there was an error getting the data from the database
     * @since 5
     */
    public static List<Order> getOrders(DatabaseConnector dbConn, boolean onlyShowActive) throws SQLException {
        final SQLException[] e = {null};
        List<Order> orders = new LinkedList<>();

        dbConn.runOnDatabase((c) -> {
            try {
                PreparedStatement ps = c.prepareStatement("select orderid, customerid, tableno, status, ordertime, lastchangetime " +
                        "from orders " + (onlyShowActive ? "where status <> " + OrderStatus.CANCELLED.getCode() + " and status <> " + OrderStatus.COMPLETE.getCode() + ";" : ";"));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    final UUID orderId = UUID.fromString(rs.getString(1));
                    final UUID custId = UUID.fromString(rs.getString(2));
                    final int tableno = rs.getInt(3);
                    final int status = rs.getInt(4);
                    final OrderStatus orderStatus = OrderStatus.from(status);
                    final LocalDateTime placedTime = rs.getTimestamp(5).toLocalDateTime();
                    final LocalDateTime lastChanged = rs.getTimestamp(6).toLocalDateTime();

                    PreparedStatement getLines = c.prepareStatement("select menuid, quantity, requests from orderlines where orderid = ?;");
                    final List<OrderLine> lines = new LinkedList<>();
                    getLines.setObject(1, orderId);
                    ResultSet lineSet = getLines.executeQuery();
                    while (lineSet.next()) {
                        final String menuId = lineSet.getString(1);
                        final int quantity = lineSet.getInt(2);
                        final String requests = lineSet.getString(3);

                        final MenuItem item = MenuItem.fromUUID(UUID.fromString(menuId), dbConn);
                        lines.add(new OrderLine(item, quantity, requests));
                    }

                    Order order = new Order(orderId, custId, lines, orderStatus, placedTime, lastChanged, tableno);
                    orders.add(order);
                }
            } catch (SQLException ex) {
                e[0] = ex;
            } catch (OrderStatusNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        if (e[0] != null) {
            throw e[0];
        }
        return orders;
    }


    /**
     * Queries the Order table in database and returns records based on a customer ID.
     *
     * @param dbConn         the database connector that stores the information
     * @param onlyShowActive whether to only show active items
     * @return a list of the orders
     * @throws SQLException thrown when there was an error getting the data from the database
     * @since 5
     */
    public static List<Order> getOrdersFromCustId(DatabaseConnector dbConn, UUID targetCustId, boolean onlyShowActive) throws SQLException {
        final SQLException[] e = {null};
        List<Order> custOrders = new LinkedList<>();

        dbConn.runOnDatabase((c) -> {
            try {
                PreparedStatement ps = c.prepareStatement("select orderid, customerid, tableno, status, ordertime, lastchangetime " +
                        "from orders where customerid = ?;");
                ps.setObject(1, targetCustId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    final UUID orderId = UUID.fromString(rs.getString(1));
                    final UUID custId = UUID.fromString(rs.getString(2));
                    final int tableno = rs.getInt(3);
                    final int status = rs.getInt(4);
                    final OrderStatus orderStatus = OrderStatus.from(status);
                    final LocalDateTime placedTime = rs.getTimestamp(5).toLocalDateTime();
                    final LocalDateTime lastChanged = rs.getTimestamp(6).toLocalDateTime();

                    PreparedStatement getLines = c.prepareStatement("select menuid, quantity, requests from orderlines where orderid = ?");
                    final List<OrderLine> lines = new LinkedList<>();
                    getLines.setObject(1, orderId);
                    ResultSet lineSet = getLines.executeQuery();
                    while (lineSet.next()) {
                        final String menuId = lineSet.getString(1);
                        final int quantity = lineSet.getInt(2);
                        final String requests = lineSet.getString(3);

                        final MenuItem item = MenuItem.fromUUID(UUID.fromString(menuId), dbConn);
                        lines.add(new OrderLine(item, quantity, requests));
                    }

                    Order custOrder = new Order(orderId, custId, lines, orderStatus, placedTime, lastChanged, tableno);
                    custOrders.add(custOrder);
                }
            } catch (SQLException ex) {
                e[0] = ex;
            } catch (OrderStatusNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        if (e[0] != null) {
            throw e[0];
        }
        return custOrders;
    }

    /**
     * Adds an order to the database, used after an order is placed.
     *
     * @param connector the database connector
     * @throws SQLException thrown when an sql error occurs during the insert operation
     * @since 4
     */
    public synchronized void addToDatabase(DatabaseConnector connector) throws SQLException, InvalidOrderException {
        SQLException[] e = {null};
        AtomicBoolean valid = new AtomicBoolean(false);
        connector.runOnDatabase((conn) -> {
            try {
                conn.setAutoCommit(false);
                double totalPrice = 0d;

                // Validate the input has a valid amount of enabled, in stock items
                for (OrderLine orderLine : orderLines) {
                    if ((!orderLine.getMenuItem().isActive())
                            || orderLine.getQuantity() <= 0
                            || (!orderLine.getMenuItem().inStock(conn))) {
                        return;
                    }

                    totalPrice += orderLine.getQuantity() * orderLine.getMenuItem().getPrice();
                    if (!orderLine.getMenuItem().inStock(conn)) {
                        throw new SQLException("Item is out of stock");
                    }

                    if (!orderLine.getMenuItem().canOrderN(conn, orderLine.getQuantity())) {
                        throw new SQLException("Cannot order the item as this will make it out of stock");
                    }
                }

                if (totalPrice < AUTO_APPROVE_THRESHOLD) {
                    this.status = OrderStatus.IN_PROGRESS;
                }

                PreparedStatement ps = conn.prepareStatement("insert into orders(orderid, customerid, tableno, status, ordertime, lastchangetime) " +
                        "values (?, ?, ?, ?, ?, ?);");
                ps.setObject(1, this.uuid);
                ps.setObject(2, this.owneruuid);
                ps.setInt(3, this.tableno);
                ps.setInt(4, this.status.getCode());
                ps.setObject(5, this.placedTime);
                ps.setObject(6, this.lastChangedTime);
                ps.execute();

                for (OrderLine orderLine : this.orderLines) {
                    PreparedStatement ops = conn.prepareStatement("insert into orderlines(orderid, menuid, quantity, requests) " +
                            "values (?, ?, ?, ?);");
                    ops.setObject(1, this.uuid);
                    ops.setObject(2, orderLine.getMenuItem().getUUID());
                    ops.setInt(3, orderLine.getQuantity());
                    ops.setString(4, orderLine.getSpecialRequest());

                    ops.execute();

                    // Decrement the stock counts
                    PreparedStatement menuinv = conn.prepareStatement("select invid, unitsrequired from inventorymenu where menuid = ?;");
                    menuinv.setObject(1, orderLine.getMenuItem().getUUID());

                    ResultSet rs = menuinv.executeQuery();
                    while (rs.next()) {
                        final UUID invid = UUID.fromString(rs.getString(1));
                        final int required = rs.getInt(2);

                        PreparedStatement updateStock = conn.prepareStatement("update inventoryitems set amount = (amount - ?) where invid = ?;");
                        updateStock.setInt(1, required * orderLine.getQuantity());
                        updateStock.setObject(2, invid);
                        if (updateStock.executeUpdate() != 1) {
                            throw new SQLException("Update failed");
                        }
                    }
                }

                valid.set(true);
                conn.commit();
            } catch (SQLException ex) {
                logger.error(ex);
                e[0] = ex;
            }
        });

        if (e[0] != null) {
            throw e[0];
        }

        if (!valid.get()) {
            throw new InvalidOrderException();
        }

        logger.info("New order " + this.uuid.toString() + " was placed.");
    }

    /**
     * Returns the uuid of the user which owns this order.
     *
     * @return the uuid of the owner
     * @since 1
     */
    public synchronized UUID getOwnerUUID() {
        return this.owneruuid;
    }

    /**
     * Gets the UUID for this order.
     *
     * @return the uuid of the order
     * @since 1
     */
    public synchronized UUID getUUID() {
        return this.uuid;
    }

    /**
     * Returns that the time that the order was placed.
     *
     * @return the time that the order was placed
     * @since 1
     */
    public synchronized LocalDateTime getPlacedTime() {
        return this.placedTime;
    }

    /**
     * Returns the time that the order was changed last
     *
     * @return the time the order was changed last
     * @since 1
     */
    public synchronized LocalDateTime getLastChangeTime() {
        return this.lastChangedTime;
    }

    /**
     * Returns the estimated prep time for an order
     *
     * @return the prep time
     * @since 6
     */
    public synchronized long getPrepTime() {
        return this.prepTime;
    }

    /**
     * Changes and sets the status of the order
     *
     * @param status    the status to change the order to
     * @param connector the connector to communicate with the database
     * @return whether the database was updated, false when an unknown error occurred
     * @throws SQLException when the database cannot be connected to
     * @since 2
     */
    public synchronized void updateStatus(final OrderStatus status, final DatabaseConnector connector) throws SQLException {
        final SQLException ex[] = {null};
        connector.runOnDatabase((conn -> {
            try {
                PreparedStatement ps = conn.prepareStatement("update orders set status = ? where orderid = ?;");
                ps.setInt(1, status.getCode());
                ps.setObject(2, this.uuid);

                ps.execute();
            } catch (SQLException e) {
                logger.error(e);
                ex[0] = e;
            }
        }));

        if (ex[0] != null) {
            throw ex[0];
        }

        // Only update on success
        this.status = status;
        this.updateLastChangedTime(connector);
    }

    /**
     * Updates the last changed time, throws SQLException on error
     *
     * @param connector the database connector used to communicate with the database
     * @return whether the database was updated, false when an unknown error occurred
     * @throws SQLException thrown when the database has an error during connection
     * @since 2
     */
    private synchronized boolean updateLastChangedTime(final DatabaseConnector connector) throws SQLException {
        LocalDateTime now = LocalDateTime.now();

        final AtomicBoolean success = new AtomicBoolean(true);
        connector.runOnDatabase((conn -> {
            try {
                PreparedStatement ps = conn.prepareStatement("update orders set lastchangetime = ? where orderid = ?;");
                ps.setObject(1, now);
                ps.setObject(2, this.uuid);

                success.set(ps.executeUpdate() == 1);
            } catch (SQLException e) {
                logger.error(e);

                success.set(false);
            }
        }));

        if (!success.get()) {
            return false;
        }

        // Only update on success
        this.lastChangedTime = now;
        return true;
    }

    /**
     * Updates an order line that is in the order.
     *
     * @param uuid         the uuid of the menu item that is being changed
     * @param newOrderLine the new orderline to replace the old one with
     * @param connector    the database connector used to communicate with the database
     * @return whether the database was updated, false when an unknown error occurred
     * @throws SQLException               thrown when the database could not be modified
     * @throws OrderLineNotFoundException the orderline could not be found
     * @throws IllegalStateException      thrown when the uuids of the new and old order do not match
     * @since 2
     */
    public synchronized boolean updateOrderLine(final UUID uuid, final OrderLine newOrderLine, final DatabaseConnector connector) throws SQLException, OrderLineNotFoundException {
        if (!uuid.equals(newOrderLine.getMenuItem().getUUID())) {
            throw new IllegalStateException("The uuids of the new order and the old order do not match.");
        }

        int i = 0;
        for (OrderLine line : this.orderLines) {
            if (line.getMenuItem().getUUID().equals(uuid)) {
                break;
            }

            i++;
        }

        if (i == this.orderLines.size()) {
            throw new OrderLineNotFoundException();
        }

        this.orderLines.remove(i);
        this.orderLines.add(newOrderLine);

        final AtomicBoolean success = new AtomicBoolean(true);
        connector.runOnDatabase((conn -> {
            try {
                PreparedStatement ps = conn.prepareStatement("update orderlines set quantity = ?, requests = ? where orderid = ? and menuid = ?;");
                ps.setInt(1, newOrderLine.getQuantity());
                ps.setString(2, newOrderLine.getSpecialRequest());
                ps.setObject(3, this.uuid);
                ps.setObject(4, newOrderLine.getMenuItem().getUUID());

                success.set(ps.executeUpdate() == 1);
            } catch (SQLException e) {
                logger.error(e);

                success.set(false);
            }
        }));

        if (!success.get()) {
            return false;
        }

        updateLastChangedTime(connector);

        return true;
    }

    /**
     * Updates the table number of an order
     *
     * @param orderid
     * @param newTable          the new table number
     * @param databaseConnector the database connector
     */
    public Boolean updateTableNo(UUID orderid, int newTable, DatabaseConnector databaseConnector) throws SQLException {

        final AtomicBoolean success = new AtomicBoolean(true);
        databaseConnector.runOnDatabase((conn -> {
            try {
                PreparedStatement ps = conn.prepareStatement("update orders set tableno = ? where orderid = ?;");
                ps.setObject(1, newTable);
                ps.setObject(2, this.uuid);

                success.set(ps.executeUpdate() == 1);
            } catch (SQLException e) {
                logger.error(e);

                success.set(false);
            }
        }));

        if (!success.get()) {
            return false;
        }

        // Only update on success
        this.tableno = newTable;
        return true;
    }

    /**
     * The hash code of an order
     *
     * @return the hash code of an order
     * @since 1
     */
    @Override
    public int hashCode() {
        return Objects.hash(uuid, owneruuid, orderLines, status, placedTime, lastChangedTime);
    }

    /**
     * Auto generated equals method, modded to ignore times as time comparison is a bit odd.
     *
     * @param o object to compare to
     * @return whether the two objects are equal
     * @since 3
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return tableno == order.tableno
                && Objects.equals(uuid, order.uuid)
                && Objects.equals(owneruuid, order.owneruuid)
                && Objects.equals(orderLines, order.orderLines)
                && status == order.status;
    }

    /**
     * Return an order item as a json object
     *
     * @return the json of the order item
     * @since 3
     */
    public synchronized JSONObject asJson() {
        JSONObject jsonObject = new JSONObject();

        JSONArray jsonArr = new JSONArray();
        this.orderLines.forEach((line) -> {
            jsonArr.add(line.asJson());
        });

        jsonObject.put("owner-uuid", this.owneruuid.toString());
        jsonObject.put("uuid", this.uuid.toString());
        jsonObject.put("order-lines", jsonArr);
        jsonObject.put("status", this.status.toString());
        jsonObject.put("placed-time", this.placedTime.toString());
        jsonObject.put("last-changed-time", this.lastChangedTime.toString());
        jsonObject.put("table-number", this.tableno);
        jsonObject.put("est-prep-time", this.prepTime);

        return jsonObject;
    }

    /**
     * Returns the order status.
     *
     * @return the order status
     * @since 3
     */
    public synchronized OrderStatus getOrderStatus() {
        return this.status;
    }

    /**
     * Gets the table number of an order
     *
     * @return the table number of an order
     * @since 2
     */
    public synchronized int getTableno() {
        return this.tableno;
    }

    /**
     * Returns an immutable copy of the order lines.
     *
     * @return an immutable copy of the order lines
     * @since 3
     */
    public synchronized List<OrderLine> getOrderLines() {
        List<OrderLine> copy = new LinkedList<>();
        this.orderLines.forEach(copy::add);

        return copy;
    }

    /**
     * Iterates arrayList orderLines, and finds the item with the longest prep time.
     *
     * @return The estimated prep time for the order (minutes)
     * @since 5
     */
    private synchronized int calculatePrepTime() {
        MenuItem mi;
        int maxTime = 0;
        for (int i = 0; i < this.orderLines.size(); i++) {
            mi = orderLines.get(i).getMenuItem();
            if (mi.getPrepTime() > maxTime) {
                maxTime = mi.getPrepTime();
            }
        }
        return maxTime;
    }

}


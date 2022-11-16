package backend.model;

import backend.database.DatabaseConnector;
import org.apache.logging.log4j.LogManager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * A waiter notification that a customer can send to the staff team.
 *
 * @author Danny
 * @version 1
 */
public class Notification {

    /**
     * Setup a logger
     *
     * @since 1
     */
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Notification.class.getName());
    /**
     * The uuid that is used in the database
     *
     * @since 1
     */
    private final UUID uuid;
    /**
     * The uuid of the customer who placed this
     *
     * @since 1
     */
    private final UUID customerUuid;
    /**
     * The body of the notification, this could be a message like more sauce or even naughty words
     *
     * @since 1
     */
    private final String body;
    /**
     * The table number of the sender, this is so the waiter knows where their customer is
     *
     * @since 1
     */
    private final int tableNumber;

    /**
     * Creates a new notification to write to the database.
     *
     * @param uuid
     * @param body
     * @param tableNumber
     * @since 1
     */
    public Notification(UUID uuid, UUID customerUuid, String body, int tableNumber) {
        this.uuid = uuid;
        this.body = body;
        this.tableNumber = tableNumber;
        this.customerUuid = customerUuid;
    }

    /**
     * Marks a notififcation as dismissed.
     *
     * @param connector the database connector
     * @param uuid      the notification's id
     * @throws SQLException
     */
    public static void rmFromDatabase(DatabaseConnector connector, UUID uuid) throws SQLException {
        SQLException[] e = {null};

        connector.runOnDatabase((conn) -> {
            try {
                PreparedStatement ps = conn.prepareStatement("update notifications set deleted = true where notif_id = ?;");
                ps.setObject(1, uuid);

                ps.execute();
            } catch (SQLException ex) {
                logger.error(ex);
                e[0] = ex;
            }
        });

        if (e[0] != null) {
            throw e[0];
        }
    }

    /**
     * Gets the UUID
     *
     * @return the notification UUID
     * @since 1
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Returns the customer who placed the order's uuid
     *
     * @return the uuid
     * @since 1
     */
    public UUID getCustomerUUID() {
        return this.customerUuid;
    }

    /**
     * Gets the body of the notification
     *
     * @return the body
     * @since 1
     */
    public String getBody() {
        return body;
    }

    /**
     * Gets the table number of the notificaiton
     *
     * @return the table number
     * @since 1
     */
    public int getTableNumber() {
        return tableNumber;
    }

    /**
     * Writes a new waiter notification to the database
     *
     * @param connector the database connector to write the notification to
     * @throws SQLException thrown when an error occurs on insert
     */
    public void addToDatabase(DatabaseConnector connector) throws SQLException {
        SQLException[] e = {null};

        connector.runOnDatabase((conn) -> {
            try {
                PreparedStatement ps = conn.prepareStatement("insert into notifications (notif_id, customerid, body, table_no, deleted) values(?, ?, ?, ?, ?);");
                ps.setObject(1, this.getUUID());
                ps.setObject(2, this.getCustomerUUID());
                ps.setString(3, this.getBody());
                ps.setInt(4, this.getTableNumber());
                ps.setBoolean(5, false);

                ps.execute();
            } catch (SQLException ex) {
                logger.error(ex);
                e[0] = ex;
            }
        });

        if (e[0] != null) {
            throw e[0];
        }
    }
}

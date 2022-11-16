package backend.model;

import backend.database.DatabaseConnector;
import org.apache.logging.log4j.LogManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Class that represents the tips stored within the database.
 *
 * @author Flynn
 * @version 2
 */
public final class Tip {
    /**
     * Setup a logger
     *
     * @since 1
     */
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Order.class.getName());
    final private UUID orderid;
    final private double amount;

    /**
     * Initialises a tip object from given data.
     *
     * @param orderid the UUID of the order a tip is associated with
     * @param amount  the amount of tip given
     * @since 1
     */
    public Tip(UUID orderid, double amount) {
        this.orderid = orderid;
        this.amount = amount;
    }

    /**
     * Returns a tip from the database for a given order ID.
     *
     * @param connector the database connector
     * @return the tip associated with an order
     * @throws SQLException thrown where a SQL errors occur during the insert operation
     * @since 2
     */
    public static Tip fromUUID(UUID orderId, DatabaseConnector connector) throws SQLException {
        final SQLException[] ex = {null};
        final List<Tip> tips = new LinkedList<>();

        connector.runOnDatabase(conn -> {
            try {
                PreparedStatement ps = conn.prepareStatement("select orderid, amount from tips where orderid = ?;");
                ps.setObject(1, orderId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    final UUID uuid = UUID.fromString(rs.getString(1));
                    final double amount = Double.parseDouble(rs.getString(2).substring(1));
                    tips.add(new Tip(orderId, amount));
                }
            } catch (SQLException e) {
                logger.error(e);
                ex[0] = e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if (ex[0] != null) {
            throw ex[0];
        }
        if (tips.size() == 0) {
            throw new SQLException("Tip Not Found");
        }
        return tips.get(0);
    }

    /**
     * Returns the UUID associated with a tip
     *
     * @return the UUID of a tip
     * @since 2
     */
    public UUID getUUID() {
        return this.orderid;
    }

    /**
     * Returns the value associated with a tip
     *
     * @return the amount of a tip
     * @since 2
     */
    public double getAmount() {
        return this.amount;
    }

    /**
     * Adds a tip to database, used when placing a tip.
     *
     * @param connector the database connector
     * @throws SQLException thrown where a SQL errors occur during the insert operation
     * @since 2
     */
    public synchronized void addToDatabase(DatabaseConnector connector) throws SQLException {
        SQLException[] e = {null};
        connector.runOnDatabase((conn) -> {
            try {
                PreparedStatement insertTip = conn.prepareStatement("insert into tips(orderid, amount) values (?, '" + this.amount + "');");
                insertTip.setObject(1, this.orderid);
                insertTip.execute();
            } catch (SQLException ex) {
                logger.error(ex);
                e[0] = ex;
            }
        });

        if (e[0] != null) {
            throw e[0];
        }

        logger.info("New tip " + this.orderid.toString() + " was given.");
    }
}

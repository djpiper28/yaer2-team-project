/*
 * A class to contain an order line which can be updated.
 */
package backend.model;

import org.json.simple.JSONObject;

import java.util.Objects;

/**
 * This class will hold an item which has been ordered and, allows for it to be updated after the
 * order is made.
 *
 * @author Flynn, Danny
 * @version 3
 * @see Order
 * @see MenuItem
 */
public final class OrderLine {
    private final MenuItem menuItem;
    private final int quantity;
    private final String specialRequest;

    /**
     * Constructor for OrderLine objects.
     *
     * @param menuItem       the item ordered
     * @param quantity       the amount of item
     * @param specialRequest special requests from the customer on the order item
     * @since 2
     */
    public OrderLine(MenuItem menuItem, int quantity, String specialRequest) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.specialRequest = specialRequest;
    }

    /**
     * Getter for MenuItem.
     *
     * @return the menu item for the order line
     * @since 1
     */
    public MenuItem getMenuItem() {
        return menuItem;
    }

    /**
     * Getter for quantity.
     *
     * @return the quantity of the menu item being ordered
     * @since 1
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Getter for special requests.
     *
     * @return the special request on a menu item.
     * @since 1
     */
    public String getSpecialRequest() {
        return specialRequest;
    }

    /**
     * Returns the hash code of an order line
     *
     * @return the hash code of an orderline
     * @since 1
     */
    @Override
    public int hashCode() {
        return Objects.hash(menuItem, quantity, specialRequest);
    }

    /**
     * Auto generated equals method
     *
     * @param o object to compare to
     * @return whether they are equal
     * @since 3
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderLine orderLine = (OrderLine) o;
        return quantity == orderLine.quantity
                && Objects.equals(menuItem, orderLine.menuItem)
                && Objects.equals(specialRequest, orderLine.specialRequest);
    }

    /**
     * Return an order line as a json object
     *
     * @return the json of the order line
     * @since 2
     */
    public JSONObject asJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("menu-item", this.menuItem.asJson());
        jsonObject.put("quantity", this.quantity);
        jsonObject.put("special-request", this.specialRequest);

        return jsonObject;
    }
}

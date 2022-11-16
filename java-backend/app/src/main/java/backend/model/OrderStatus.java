/*
 * The status of an order which a customer has made.
 */
package backend.model;

import backend.exceptions.OrderStatusNotFoundException;

/**
 * The status of an order that has been placed.
 *
 * @author Danny, Flynn
 * @version 4
 */
public enum OrderStatus {
    /**
     * An order which has been placed, but the kitchen has not confirmed.
     *
     * @since 1
     */
    UNCONFIRMED(0),

    /**
     * An order which has been confirmed by the kitchen and is in progress.
     *
     * @since 1
     */
    IN_PROGRESS(1),

    /**
     * An order which has been prepared by the kitchen and ready for delivery.
     *
     * @since 1
     */
    COMPLETE(2),

    /**
     * An order which has been delivered to customer.
     *
     * @since 4
     */
    DELIVERED(3),

    /**
     * An order which a customer cancelled.
     *
     * @since 4
     */
    CANCELLED(4);

    /**
     * The status code that is used in the database
     *
     * @since 2
     */
    private final int status;

    /**
     * Inits an order status with the status code
     *
     * @param s the status code
     * @since 2
     */
    private OrderStatus(int s) {
        this.status = s;
    }

    /**
     * Return the order status that the int represents
     *
     * @param i the database code for the order status
     * @return the order status that is represented by the integer
     * @throws OrderStatusNotFoundException
     * @since 3
     */
    public static OrderStatus from(int i) throws OrderStatusNotFoundException {
        for (OrderStatus s : OrderStatus.values()) {
            if (s.getCode() == i) {
                return s;
            }
        }

        throw new OrderStatusNotFoundException();
    }

    /**
     * Returns the code that represents this status in the database.
     *
     * @return the code that is used for this status in the database
     * @since 2
     */
    public int getCode() {
        return this.status;
    }
}

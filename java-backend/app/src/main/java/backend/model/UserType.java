package backend.model;

/**
 * The type of user, this is used for security such that they cannot access the wrong end points.
 *
 * @author Danny, John
 * @version 2
 */
public enum UserType {

    /**
     * A customer of the application can order, edit their order and, pay.
     *
     * @since 2
     */
    CUSTOMER(0),

    /**
     * A kitchen staff can confirm, cancel and, mark orders as complete.
     *
     * @since 2
     */
    KITCHEN_STAFF(1),

    /**
     * A waiter staff can place orders, modify and, cancel them.
     *
     * @since 2
     */
    WAITER_STAFF(2);

    /**
     * The code that is stored in the database to represent this user.
     *
     * @since 2
     */
    private final int code;

    /**
     * Inits a user type with the database code.
     *
     * @param i the enum code for the database
     * @since 2
     */
    private UserType(int i) {
        this.code = i;
    }

    public static UserType from(int i) {
        for (UserType u : UserType.values()) {
            if (u.getCode() == i) {
                return u;
            }
        }

        throw new IllegalArgumentException(String.format("%d is not a user type", i));
    }

    /**
     * Return the code that represents the user type in the database.
     *
     * @return the code that represents this enum in the database.
     * @since 2
     */
    public int getCode() {
        return code;
    }

}

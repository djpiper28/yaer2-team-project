package backend.model;

import org.json.simple.JSONObject;

import java.util.UUID;

/**
 * A model of a user of the application
 *
 * @author Danny
 * @version 3
 */
public final class User {
    private final UUID uuid;
    private final String firstname, surname, email, phoneNumber;
    private final UserType userType;

    /**
     * Inits a user.
     *
     * @param uuid        the uuid of the user
     * @param firstname   the user's firstname
     * @param surname     the user's surname
     * @param email       the email of the user
     * @param phoneNumber the phone number of the user
     * @param userType    the type of the user
     * @since 1
     */
    public User(UUID uuid, String firstname, String surname, String email, String phoneNumber, UserType userType) {
        this.uuid = uuid;
        this.firstname = firstname;
        this.surname = surname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
    }

    /**
     * A nice wrapper to make a customer and assert that the data is sanitised and valid
     * *
     *
     * @param firstname   the firstname of the user
     * @param surname     the surname of the user
     * @param email       the email of the user
     * @param phoneNumber the phone number of the user
     * @return the user object
     * @since 3
     */
    public static User createNewCustomer(String firstname, String surname, String email, String phoneNumber) {
        final UUID uuid = UUID.randomUUID();
        final UserType type = UserType.CUSTOMER;

        // TODO: Validate and sanitise input

        return new User(uuid, firstname, surname, email, phoneNumber, type);
    }

    /**
     * Returns a json representation of the user.
     *
     * @return a json representation of the user
     * @since 3
     */
    public JSONObject asJson() {
        final JSONObject object = new JSONObject();
        object.put("uuid", this.uuid.toString());
        object.put("firstname", this.firstname);
        object.put("surname", this.surname);
        object.put("email", this.email);
        object.put("phonenumber", this.phoneNumber);
        object.put("user-type", this.userType.toString());

        return object;
    }

    /**
     * Returns the uuid of the user
     *
     * @return the uuid of the user
     * @since 1
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Returns the user's first name
     *
     * @return the firstname of the user
     * @since 1
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * Returns the suer's surname
     *
     * @return the surname of the user
     * @since 1
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Returns the user's email
     *
     * @return the email of the user
     * @since 1
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the user's phone number
     *
     * @return the phone number of the user
     * @since 1
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Reutrns the type of the user
     *
     * @return the type of user
     * @since 1
     */
    public UserType getUserType() {
        return userType;
    }
}

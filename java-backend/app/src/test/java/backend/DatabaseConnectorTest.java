/*
 * Tests that the database connector has the correct behaviour.
 */
package backend;

import java.sql.SQLException;
import java.lang.RuntimeException;

import backend.database.DatabaseConnector;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectorTest {
    public static final String URL = "jdbc:postgresql://www.djpiper28.co.uk:6445/REDACTED",
                               USERNAME = "REDACTED",
                               PASSWORD = "REDACTED";
    
    @Test void testConstructor() {
        DatabaseConnector db = new DatabaseConnector("url", "username", "password");
    }

    @Test void testDbRunnerException() throws SQLException {
        // Test connection failure
        final DatabaseConnector db = new DatabaseConnector(URL, USERNAME, PASSWORD + "WRONG PASSWORD");
        final Exception[] ex = {null};
        assertThrows(SQLException.class, () -> {
            db.runOnDatabase((conn) -> {
                /* Do nothing */
            });                                                
        });

        // Test runtime error is caught and true is returned
        final DatabaseConnector db2 = new DatabaseConnector(URL, USERNAME, PASSWORD);
        final Exception[] ex2 = {null};
        boolean res = db2.runOnDatabase((conn) -> {
            throw new RuntimeException("Test error handling");
        });
        
        assertTrue(res);

        // Assert that successful operations throw no warnings
        assertFalse(db2.runOnDatabase((conn) -> {}));
    }

    @Test void testDbRunnerGoodCase() throws Exception {
        final DatabaseConnector db = TestUtils.getDatabaseConnector();
        db.runOnDatabase((conn) -> {});
    }
    
}

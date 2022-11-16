package backend;

import backend.database.DatabaseConnector;

public class TestUtils {

  /**
   * Constansts to connect to database.
   */
  public static final String URL = "jdbc:postgresql://www.djpiper28.co.uk:6445/testenv",
      USERNAME = "dev",
      PASSWORD = "REDACTED";
  private static DatabaseConnector conn = new DatabaseConnector(URL, USERNAME, PASSWORD);

  public static DatabaseConnector getDatabaseConnector() {
    return conn;
  }
}

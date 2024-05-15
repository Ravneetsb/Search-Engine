package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

/**
 * This class is designed to test your database configuration. You need to have
 * a database.properties file with username, password, database, and hostname.
 * You must also have the tunnel to stargate.cs.usfca.edu running if you are
 * off-campus.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class DatabaseConnector {
    /**
     * URI to use when connecting to database. Should be in the format:
     * jdbc:subprotocol://hostname/database
     */
    public final String uri;

    /** Properties with username and password for connecting to database. */
    private final Properties login;

    public static final String INSERT_SEARCHES = "insert into queries (query, count) values (?, 1) on duplicate key update count = count + 1;";

    public static final String GET_TOP_FIVE_SEARCHES = "select query, count from queries order by count desc limit 5;";

    /**
     * Creates a connector from a "database.properties" file located in the
     * current working directory.
     *
     * @throws IOException if unable to properly parse properties file
     * @throws FileNotFoundException if properties file not found
     */
    public DatabaseConnector() throws FileNotFoundException, IOException {
        this(Path.of("database.properties"));
    }

    /**
     * Creates a connector from the provided database properties file.
     *
     * @param path path to the database properties file
     * @throws IOException if unable to properly parse properties file
     * @throws FileNotFoundException if properties file not found
     */
    public DatabaseConnector(Path path) throws FileNotFoundException, IOException {
        // Try to load the configuration from file
        Properties config = loadConfig(path);

        // Create database URI in proper format
        uri = String.format("jdbc:mariadb://%s/%s",
                config.getProperty("hostname"),
                config.getProperty("database"));

        // Create database login properties
        login = new Properties();
        login.put("user", config.getProperty("username"));
        login.put("password", config.getProperty("password"));
    }

    /**
     * Attempts to load properties file with database configuration. Must include
     * username, password, database, and hostname.
     *
     * @param path path to database properties file
     * @return database properties
     * @throws IOException if unable to properly parse properties file
     * @throws FileNotFoundException if properties file not found
     */
    public static Properties loadConfig(Path path) throws FileNotFoundException, IOException {
        // Specify which keys must be in properties file
        Set<String> required = new HashSet<>();
        required.add("username");
        required.add("password");
        required.add("database");
        required.add("hostname");

        // Load properties file
        Properties config = new Properties();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            config.load(reader);
        }

        // Check that required keys are present
        if (!config.keySet().containsAll(required)) {
            String error = "Must provide the following in properties file: ";
            throw new InvalidPropertiesFormatException(error + required);
        }

        return config;
    }

    /**
     * Attempts to connect to database using loaded configuration.
     *
     * @return database connection
     * @throws SQLException if unable to establish database connection
     */
    public Connection getConnection() throws SQLException {
        Connection dbConnection = DriverManager.getConnection(uri, login);
        dbConnection.setAutoCommit(true);
        return dbConnection;
    }

    public void insertSearch(Connection db, String query) throws SQLException {
        try (PreparedStatement  statement =  db.prepareStatement(INSERT_SEARCHES)) {
            statement.setString(1, query);
            statement.execute();
        }
    }

    public List<String> getTopFiveSearches(Connection db) throws SQLException {
        ArrayList<String> topFive = new ArrayList<>();
        try (PreparedStatement statement = db.prepareStatement(GET_TOP_FIVE_SEARCHES)) {
            var result = statement.executeQuery();
            while (result.next()) {
                String query = result.getString(1);
                String count = result.getNString(2);
                topFive.add(String.join(" ", query, ": ", count, " searches"));
            }
        }
        return topFive;
    }

    public void resetMetaData(Connection db) throws SQLException {
        try (Statement statement = db.createStatement()) {
            statement.execute("delete * from queries");
        }
    }

    /**
     * Opens a database connection and returns a set of found tables. Will return
     * an empty set if there are no results.
     *
     * @param db the active database connection
     *
     * @return set of tables
     * @throws SQLException if unable to execute SQL
     */
    public Set<String> getTables(Connection db) throws SQLException {
        Set<String> tables = new HashSet<>();

        // Create statement and close when done.
        // Database connection will be closed elsewhere.
        try (Statement sql = db.createStatement();) {
            if (sql.execute("SHOW TABLES;")) {
                ResultSet results = sql.getResultSet();

                while (results.next()) {
                    tables.add(results.getString(1));
                }
            }
        }

        return tables;
    }

    /**
     * Opens a database connection, executes a simple statement, and closes the
     * database connection.
     *
     * @return true if all operations successful
     */
    public boolean testConnection() {
        boolean okay = false;

        // Open database connection and close when done
        try (Connection db = getConnection();) {
            insertSearch(db, "a");
            System.out.println("Executing SHOW TABLES...");
            Set<String> tables = getTables(db);

            if (tables != null) {
                System.out.print("Found " + tables.size() + " tables: ");
                System.out.println(tables);
                okay = true;
            }
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return okay;
    }

    /**
     * Tests whether database configuration (including tunnel) is correct. If you
     * see the message "Connection to database established" and the tables within
     * your database, then your settings are correct.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        try {
            Path base = Path.of("src", "main", "resources");
            Path properties = base.resolve("database.properties");

            // Check for other properties file in command-line arguments
            if (args.length > 0) {
                properties = base.resolve(args[0]);
            }

            System.out.println("Loading " + properties + " ...");

            DatabaseConnector test = new DatabaseConnector(properties);
            System.out.println("Connecting to " + test.uri);

            if (test.testConnection()) {
                System.out.println("Connection to database established.");
            }
            else {
                System.err.println("Unable to connect properly to database.");
            }
        }
        catch (Exception e) {
            System.err.println("Unable to connect properly to database.");
            System.err.println(e.getMessage());
        }
    }
}


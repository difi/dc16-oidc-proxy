package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.DefaultProxyCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

public class CookieDatabase {
    private final String JDBC_DRIVER = "org.h2.Driver";
    private final String DB_URL = "jdbc:h2:mem:cookie";
    private final String USER = "SA";
    private final String PASS = "";

    private static Logger logger = LoggerFactory.getLogger(CookieDatabase.class);
    private static final int MINUTE = 60 * 1000;

    private Statement statement;
    private ResultSet resultSet;

    /**
     * Starting the database with specified driver, URL, user and password
     */
    public CookieDatabase() {
        try {
            Class.forName(JDBC_DRIVER);
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Exception caught in CookieDatabase.CookieDatabase(): " + e);
            e.printStackTrace();
        }
    }

    /**
     * Creates a table in the database for storing cookies.
     * <p>
     * Columns:
     * UUID - String of a 128 bit, type 4 (pseudo randomly generated) Universally Unique ID
     * name - String of the cookie's name, specific to the host
     * host - String of the hostname
     * path - String of the path, starting with "/"
     * touchPeriod - int value of amount of minutes a session is initially valid for
     * maxExpiry - int value of amount of minutes a session is maximum valid for, from creation of cookie
     * userData - String of a HashMap.toString() storing the JWT received from the authorization server
     * created - BigInt (long) of created's Date.getTime() method, indicating creation-time in milliseconds
     * lastUpdated - BigInt (long) of lastUpdated's Date.getTime() method, indicating creation-time in milliseconds
     */
    public void createTable() {
        try {
            statement.execute("CREATE TABLE IF NOT EXISTS PUBLIC.cookie " +
                    "(" +
                    "uuid VARCHAR(36) PRIMARY KEY NOT NULL, " +
                    "name VARCHAR(30) NOT NULL, " +
                    "host VARCHAR(30) NOT NULL, " +
                    "path VARCHAR(30) NOT NULL, " +
                    "touchPeriod INT NOT NULL, " +
                    "maxExpiry INT NOT NULL, " +
                    "userData VARCHAR(500), " +
                    "created BIGINT NOT NULL, " +
                    "lastUpdated BIGINT NOT NULL" +
                    ");");
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS \"cookie_uuid_uindex\" ON PUBLIC.cookie (uuid);");
        } catch (SQLException e) {
            System.err.println("SQLException caught in CookieDatabase.createTable(): " + e);
            e.printStackTrace();
        }
        System.out.println("\nDB: Database initialized with cookie table\n");
    }

    /**
     * Takes in a ProxyCookie object and inputs it into the database. Sets the lastUpdated value to present time.
     *
     * @param cookie ProxyCookie
     */
    public void insertCookie(ProxyCookie cookie) {
        //long now = new Date().getTime(); // lastUpdated
        String userData;
        if (cookie.getUserData() == null) userData = null;
        else userData = cookie.getUserData().toString();
        String query = String.format("INSERT INTO PUBLIC.cookie (uuid, name, host, path, touchPeriod, maxExpiry, userData, created, lastUpdated) " +
                        "VALUES ('%s','%s','%s','%s','%s','%s', '%s', '%s', '%s');", cookie.getUuid(), cookie.getName(), cookie.getHost(),
                cookie.getPath(), cookie.getTouchPeriod(), cookie.getMaxExpiry(), userData,
                cookie.getCreated().getTime(), cookie.getLastUpdated().getTime());
        //System.out.println("DB: Insert cookie query: " + query);
        try {
            statement.executeUpdate(query);
            //System.out.println("DB: Cookie inserted into the database (" + cookie + ")");
        } catch (SQLException e) {
            System.err.println("SQLException caught in CookieDatabase.insertCookie(): " + e);
            e.printStackTrace();
        }
    }

    /**
     * UserData is current saved in the database as VARCHAR(400) with value HashMaps.toString().
     * This method is used for converting the String back til a HashMap.
     *
     * @param str toString of HashMap
     * @return HashMap
     */
    public static HashMap<String, String> stringToHashMap(String str) {
        // If HashMap is empty (only containing "{}"), the object should be null
        if (str == null || str.equals("null") || str.equals("{}")) return null;
        // Removing curly braces, spaces and escape characters
        String keyValues = str.replaceAll("[\\s\\{\\}]+", "");
        HashMap<String, String> hashMap = new HashMap<>();
        for (String pair : keyValues.split(",")) {
            String[] elem = pair.split("=");
            hashMap.put(elem[0], elem[1]);
        }
        return hashMap;
    }

    /**
     * Looks up entry with given uuid in the database. Creates a cookie object with it's created and lastUpdated
     * values, later using these to validate it's validity. Returns an empty Optional if exception is caught or
     * cookie is not found.
     *
     * @param uuid String
     * @return Optional<ProxyCookie>
     */
    public Optional<ProxyCookie> findCookie(String uuid) {
        ProxyCookie cookie = null;
        try {
            resultSet = statement.executeQuery("SELECT * from PUBLIC.cookie WHERE uuid = '" + uuid + "';");
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String host = resultSet.getString("host");
                String path = resultSet.getString("path");
                int touchPeriod = resultSet.getInt("touchPeriod");
                int maxExpiry = resultSet.getInt("maxExpiry");
                // Handles empty userData HashMap "{}" in help-method, setting it to null
                HashMap<String, String> userData = stringToHashMap(resultSet.getString("userData"));
                Date created = new Date(resultSet.getLong("created"));
                Date lastUpdated = new Date(resultSet.getLong("lastUpdated"));

                cookie = new DefaultProxyCookie(uuid, name, host, path, touchPeriod, maxExpiry, userData, created, lastUpdated);

                //System.out.println("\nDB: Found cookie in database (" + cookie + ")");
            }
        } catch (SQLException e) {
            System.err.println("SQLException caught in CookieDatabase.findCookie(): " + e);
            e.printStackTrace();
        }
        return Optional.ofNullable(cookie);
    }

    /**
     * Mostly for debug purposes, but might serve other purpose later. Retrieves entries in cookie database,
     * instantiates ProxyCookie objects of the entries, and returns them as objects in a HashMap with the
     * cookie's uuid as key.
     *
     * @return HashMap
     */
    public HashMap<String, ProxyCookie> getAllCookies() {
        HashMap<String, ProxyCookie> cookies = new HashMap<>();
        System.out.println("\ngetAllCookies()");
        try {
            resultSet = statement.executeQuery("SELECT * FROM PUBLIC.cookie;");
            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                String name = resultSet.getString("name");
                String host = resultSet.getString("host");
                String path = resultSet.getString("path");
                int touchPeriod = resultSet.getInt("touchPeriod");
                int maxExpiry = resultSet.getInt("maxExpiry");
                // Handles empty userData HashMap "{}" in stringToHashMap(), setting it to null
                HashMap<String, String> userData = stringToHashMap(resultSet.getString("userData"));
                Date created = new Date(resultSet.getLong("created"));
                Date lastUpdated = new Date(resultSet.getLong("lastUpdated"));

                System.out.printf("\n%.65s %15s %20s %20s %5s %5s %20s %20s %40s", uuid, name, host,
                        path, touchPeriod, maxExpiry, created.toString(), lastUpdated.toString(), userData);
                cookies.put(uuid, new DefaultProxyCookie(uuid, name, host, path, touchPeriod, maxExpiry, userData, created, lastUpdated));
            }
        } catch (SQLException e) {
            System.err.println("SQLException caught in CookieDatabase.getAllCookies(): " + e);
            e.printStackTrace();
        }
        return cookies;
    }


    /**
     * Removes all expired cookies from the database. Currently only deletes cookies based on
     * expiry from lastUpdated + touchPeriod. Cases where (lastUpdated + touchPeriod) >
     * (created + maxExpiry) is validated in DatabaseCookieStorage.findCookie() before cookie
     * is returned.
     */
    public void removeExpiredCookies() {
        try {
            long now = new Date().getTime(); // present time in milliseconds
            String expiredEntries = "SELECT uuid FROM PUBLIC.cookie WHERE (lastUpdated + touchPeriod * " + MINUTE + ") < " + now;
            //String maxExpiredEntries = "SELECT uuid FROM PUBLIC.cookie WHERE (created + maxExpiry * "+MINUTE+") < " + now;
            statement.executeUpdate("DELETE FROM PUBLIC.cookie WHERE uuid IN (" + expiredEntries + ");");
            System.out.println("\nDB: Expired cookies removed from database");
        } catch (SQLException e) {
            System.err.println("SQLException caught in CookieDatabase.removeExpiredCookies(): " + e);
            e.printStackTrace();
        }
    }

    /**
     * Extends expiry of cookie with given UUID, by updating the lastUpdated value. Doesn't need
     * to handle cases where (lastUpdated + touchPeriod) > (created + maxExpiry), as this is
     * validated in DatabaseCookieStorage.findCookie() before cookie is returned.
     *
     * @param uuid String
     */
    public void extendCookieExpiry(String uuid, Date lastUpdated) {
        try {
            long now = lastUpdated.getTime(); // present time in milliseconds
            String query = "UPDATE PUBLIC.cookie SET lastUpdated = " + now + " WHERE uuid = '" + uuid + "';";
            statement.executeUpdate(query);
            //System.out.println("\nDB: Updated expiry of cookie (UUID: " + uuid + ")");
        } catch (SQLException e) {
            System.err.println("SQLException caught in CookieDatabase.touchCookie(): " + e);
            e.printStackTrace();
        }
    }

    // Debug
    public static void printCookie(ProxyCookie cookie) {
        System.out.println("\ncookie.toString(): " + cookie);
        System.out.println("cookie.getName(): " + cookie.getName());
        System.out.println("cookie.getHost(): " + cookie.getHost());
        System.out.println("cookie.getPath(): " + cookie.getPath());
        System.out.println("cookie.getTouchPeriod(): " + cookie.getTouchPeriod());
        System.out.println("cookie.getMaxExpiry(): " + cookie.getMaxExpiry());
        System.out.println("cookie.getUserData(): " + cookie.getUserData());
        System.out.println("cookie.getCreated(): " + cookie.getCreated());
        System.out.println("cookie.getLastUpdated(): " + cookie.getLastUpdated() + "\n");
    }

}

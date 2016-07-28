package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CookieDatabase {

    private static final String JDBC_DRIVER = "org.h2.Driver";

    private static final String DB_URL = "jdbc:h2:mem:cookie";

    private static final String USER = "SA";

    private static final String PASS = "";

    private static Logger logger = LoggerFactory.getLogger(CookieDatabase.class);

    private static final int MINUTE = 60 * 1000;

    private Statement statement;

    private ResultSet resultSet;

    /**
     * Starting the database with specified driver, URL, user and password
     */
    public CookieDatabase() {
        System.out.println("CookieDatabase.constructor");
        try {
            Class.forName(JDBC_DRIVER);
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            createTable();
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Exception caught in CookieDatabase.CookieDatabase(): {}", e.getMessage(), e);
        }
    }

    /**
     * Creates a table in the database for storing cookies.
     * <p>
     * Columns:
     * UUID - String of a 128 bit, type 4 (pseudo randomly generated) Universally Unique ID
     * name - String of the cookie's name, specific to the host
     * host - String of the hostname
     * idp - String of the idp
     * touchPeriod - int value of amount of minutes a session is initially valid for
     * maxExpiry - int value of amount of minutes a session is maximum valid for, from creation of cookie
     * userData - String of a HashMap.toString() storing the JWT received from the authorization server
     * created - BigInt (long) of created's Date.getTime() method, indicating creation-time in milliseconds
     * lastUpdated - BigInt (long) of lastUpdated's Date.getTime() method, indicating creation-time in milliseconds
     */
    public void createTable() {
        System.out.println("CookieDatabase.createTable()");
        try {
            statement.execute("CREATE TABLE IF NOT EXISTS PUBLIC.cookie " +
                "(" +
                "uuid VARCHAR(36) NOT NULL, " +
                "name VARCHAR(30) NOT NULL, " +
                "host VARCHAR(30) NOT NULL, " +
                "idp VARCHAR(30) NOT NULL, " +
                "security INT NOT NULL, " +
                "touchPeriod INT NOT NULL, " +
                "maxExpiry INT NOT NULL, " +
                "userData VARCHAR(500), " +
                "created BIGINT NOT NULL, " +
                "lastUpdated BIGINT NOT NULL, " +
                "PRIMARY KEY (uuid, idp) " +
                ");");
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS \"cookie_uuid_uindex\" ON PUBLIC.cookie (uuid);");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("SQLException caught in CookieDatabase.createTable(): {}", e.getMessage(), e);
        }
        logger.debug("DB: Database initialized with cookie table");
    }

    /**
     * Takes in a ProxyCookie object and inputs it into the database. Sets the lastUpdated value to present time.
     *
     * @param cookie ProxyCookie
     */
    public void insertCookie(ProxyCookie cookie) {
        System.out.println("CookieDatabase.insertCookie");
        String userData;
        if (cookie.getUserData() == null || cookie.getUserData().toString().equals("{}")) userData = null;
        else userData = cookie.getUserData().toString();
        String query = String.format("INSERT INTO PUBLIC.cookie (uuid, name, host, idp, security, touchPeriod, maxExpiry, userData, created, lastUpdated) " +
                        "VALUES ('%s','%s','%s','%s', %s, %s, %s, '%s', %s, %s);", cookie.getUuid(), cookie.getName(), cookie.getHost(),
                cookie.getIdp(), cookie.getSecurity(), cookie.getTouchPeriod(), cookie.getMaxExpiry(),
                userData, cookie.getCreated().getTime(), cookie.getLastUpdated().getTime());
        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.warn("SQLException caught in CookieDatabase.insertCookie(): {}", e.getMessage(), e);
        }
    }

    /**
     * DefaultUserData is current saved in the database as VARCHAR(400) with value HashMaps.toString().
     * This method is used for converting the String back til a HashMap.
     *
     * @param str toString of HashMap
     * @return HashMap
     */
    public static Map<String, String> stringToMap(String str) {
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
        System.out.println("CookieDatabase.findCookie(String "+ uuid + ")");
        ProxyCookie cookie = null;
        try {
            resultSet = statement.executeQuery("SELECT * from PUBLIC.cookie WHERE uuid = '" + uuid + "';");
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String host = resultSet.getString("host");
                String idp = resultSet.getString("idp");
                int security = resultSet.getInt("security");
                int touchPeriod = resultSet.getInt("touchPeriod");
                int maxExpiry = resultSet.getInt("maxExpiry");
                Map<String, String> userData = stringToMap(resultSet.getString("userData"));
                Date created = new Date(resultSet.getLong("created"));
                Date lastUpdated = new Date(resultSet.getLong("lastUpdated"));
                System.err.println("\nUserData:\n"+userData);
                cookie = new DefaultProxyCookie(uuid, name, host, idp, security, touchPeriod, maxExpiry, userData, created, lastUpdated);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.warn("SQLException caught in CookieDatabase.findCookie(): {}", e.getMessage(), e);
        }
        return Optional.ofNullable(cookie);
    }

    public void removeCookie(String uuid) {
        System.out.println("CookieDatabase.removeCookie(String "+uuid+")");
        try {
            statement.executeUpdate("DELETE FROM PUBLIC.cookie WHERE uuid = '" + uuid + "';");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.warn("SQLException caught in CookieDatabase.removeCookie()");
            e.printStackTrace();
        }
    }

    /**
     * Mostly for debug purposes, but might serve other purpose later. Retrieves entries in cookie database,
     * instantiates ProxyCookie objects of the entries, and returns them as objects in a HashMap with the
     * cookie's uuid as key.
     *
     * @return HashMap
     */
    public Map<String, ProxyCookie> getAllCookies() {
        System.out.println("CookieDatabase.getAllCookies()");
        HashMap<String, ProxyCookie> cookies = new HashMap<>();
        try {
            resultSet = statement.executeQuery("SELECT * FROM PUBLIC.cookie;");
            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                String name = resultSet.getString("name");
                String host = resultSet.getString("host");
                String idp = resultSet.getString("idp");
                int security = resultSet.getInt("security");
                int touchPeriod = resultSet.getInt("touchPeriod");
                int maxExpiry = resultSet.getInt("maxExpiry");
                // Handles empty userData HashMap "{}" in stringToMap(), setting it to null
                Map<String, String> userData = stringToMap(resultSet.getString("userData"));
                Date created = new Date(resultSet.getLong("created"));
                Date lastUpdated = new Date(resultSet.getLong("lastUpdated"));

                cookies.put(uuid, new DefaultProxyCookie(uuid, name, host, idp, security, touchPeriod, maxExpiry, userData, created, lastUpdated));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.warn("SQLException caught in CookieDatabase.getAllCookies(): {}", e.getMessage(), e);
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
            statement.executeUpdate("DELETE FROM PUBLIC.cookie WHERE uuid IN (" + expiredEntries + ");");
            logger.info("DB: Expired cookies removed from database");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.warn("SQLException caught in CookieDatabase.removeExpiredCookies(): {}", e.getMessage(), e);
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
        System.out.println("CookieDatabase.extendCookieExpiry()");
        try {
            long now = lastUpdated.getTime(); // present time in milliseconds
            String query = "UPDATE PUBLIC.cookie SET lastUpdated = " + now + " WHERE uuid = '" + uuid + "';";
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.warn("SQLException caught in CookieDatabase.touchCookie(): {}", e.getMessage(), e);
        }
    }

    // Debug
    public static void printCookie(ProxyCookie cookie) {
        System.out.println("\ncookie.toString(): " + cookie);
        System.out.println("cookie.getName(): " + cookie.getName());
        System.out.println("cookie.getHost(): " + cookie.getHost());
        System.out.println("cookie.getIdp(): " + cookie.getIdp());
        System.out.println("cookie.getSecurity(): " + cookie.getSecurity());
        System.out.println("cookie.getTouchPeriod(): " + cookie.getTouchPeriod());
        System.out.println("cookie.getMaxExpiry(): " + cookie.getMaxExpiry());
        System.out.println("cookie.getUserData(): " + cookie.getUserData());
        System.out.println("cookie.getCreated(): " + cookie.getCreated());
        System.out.println("cookie.getLastUpdated(): " + cookie.getLastUpdated() + "\n");
    }

}

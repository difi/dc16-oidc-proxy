package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.DefaultProxyCookie;
import org.h2.tools.Server;

import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class CookieDatabase {
    private final String JDBC_DRIVER = "org.h2.Driver";
    private final String DB_URL = "jdbc:h2:mem:cookie";
    private final String USER = "SA";
    private final String PASS = "";

    Server server;
    Connection connection;
    Statement statement;
    ResultSet resultSet;

    public CookieDatabase() {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
        } catch (SQLException | ClassNotFoundException e){
            System.err.println("Exception caught in CookieDatabase.CookieDatabase(): " + e);
            e.printStackTrace();
        }
    }

    public void createTable(){
        try{
            statement.execute("CREATE TABLE IF NOT EXISTS PUBLIC.cookie " +
                    "(" +
                        "uuid VARCHAR(36) PRIMARY KEY NOT NULL, " +
                        "name VARCHAR(30) NOT NULL, " +
                        "host VARCHAR(30) NOT NULL, " +
                        "path VARCHAR(30) NOT NULL, " +
                        "expiry BIGINT NOT NULL, " +
                        "maxExpiry BIGINT NOT NULL, " +
                        "userData BLOB" +
//                    "    created BIGINT NOT NULL,\n" +
//                    "    lastUpdated BIGINT NOT NULL\n" +
                    ");");
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS \"cookie_uuid_uindex\" ON PUBLIC.cookie (uuid);");
        } catch (SQLException e){
            System.err.println("SQLException caught in CookieDatabase.createTable(): " + e);
            e.printStackTrace();
        } System.out.println("DB: Table 'PUBLIC.cookie' created in H2 database");
    }

    public void insertCookie(ProxyCookie cookie){
        // For Cookie variables expiry and maxExpiry, Date's getTime() is used to store millisecond values in the database as BIGINT
        String query = String.format("INSERT INTO PUBLIC.cookie (uuid, name, host, path, expiry, maxExpiry) " +
                        "VALUES ('%s','%s','%s','%s','%s','%s')", cookie.getUuid(), cookie.getName(), cookie.getHost(),
                cookie.getPath(), cookie.getExpiry().getTime(), cookie.getMaxExpiry().getTime());

        /*
        Unclear at current time whether lastUpdated and created should be variables in cookies and database
        Also, query above includes name and path
        String query = String.format("INSERT INTO PUBLIC.cookie (uuid, host, expiry, maxExpiry, created, lastUpdated) " +
                        "VALUES ('%s','%s','%s','%s','%s','%s')", cookie.getUuid(), cookie.getHost(), cookie.getExpiry().getTime(),
                        cookie.getMaxExpiry().getTime(), cookie.getCreated().getTime(), cookie.getLastUpdated().getTime());
        */
        System.out.println("DB: Insert cookie query: " + query);
        try {
            statement.execute(query);
        } catch (SQLException e){
            System.err.println("SQLException caught in CookieDatabase.insertCookie(): " + e);
            e.printStackTrace();
        } System.out.println("DB: Cookie inserted into the database with uuid " + cookie.getUuid());
    }
    public Optional<ProxyCookie> findCookie(String uuid){
        ProxyCookie cookie = null;
        try {
            resultSet = statement.executeQuery("SELECT * from PUBLIC.cookie WHERE uuid = '"+uuid+"'");
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String host = resultSet.getString("host");
                String path = resultSet.getString("path");
                long expiry = resultSet.getLong("expiry");
                long maxExpiry = resultSet.getLong("maxExpiry");
                Object userData = resultSet.getObject("userData");

                // TODO: Handle conversion of userData object to HashMap<String, String>. Returns null object in ProxyCookie constructor until resolved

                System.out.printf("%s65 %15s %20s %20s %20s %20s %20s\n", uuid, name, host, path, expiry, maxExpiry, userData);
                cookie = new DefaultProxyCookie(uuid, name, host, path, new Date(expiry), new Date(maxExpiry), null);
                System.out.println("DB: Found cookie with uuid " + uuid);
            } else {
                System.err.println("DB: Cookie with this uuid does not exist: "+uuid);
            }
        }catch (SQLException e){
            System.err.println("SQLException triggered in CookieDatabase.findCookie(): " + e);
            e.printStackTrace();
        }
        return Optional.ofNullable(cookie);
    }

    public static void main(String[] args) {
        CookieDatabase db = new CookieDatabase();
        db.createTable();

        // Creating test entries
        db.insertCookie(new DefaultProxyCookie("test-cookie", "name", "host.com", "/", new Date(new Date().getTime() + 30 * 60 * 1000), new Date(new Date().getTime() + 120 * 60 * 1000), new HashMap<>(1)));
        for (int i=1; i<5; i++){
            db.insertCookie(new DefaultProxyCookie(UUID.randomUUID().toString(), "name"+i, "host.com", "/", new Date(new Date().getTime() + 30 * 60 * 1000), new Date(new Date().getTime() + 120 * 60 * 1000), new HashMap<>(1)));
        }

        // Finding a test entry
        Optional<ProxyCookie> testCookie = db.findCookie("test-cookie");
        if (testCookie.isPresent()){
            System.out.println("testCookie.get(): "+testCookie.get());
            System.out.println("testCookie.get().getName(): "+testCookie.get().getName());
            System.out.println("testCookie.get().getHost(): "+testCookie.get().getHost());
            System.out.println("testCookie.get().getPath(): "+testCookie.get().getPath());
            System.out.println("testCookie.get().getExpiry(): "+testCookie.get().getExpiry());
            System.out.println("testCookie.get().getMaxExpiry(): "+testCookie.get().getMaxExpiry());
        }

    }
}

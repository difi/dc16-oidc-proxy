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
            statement.execute("CREATE TABLE IF NOT EXISTS PUBLIC.cookie\n" +
                    "(\n" +
                    "    uuid VARCHAR(36) PRIMARY KEY NOT NULL,\n" +
                    "    userData BLOB,\n" +
                    "    host VARCHAR(30) NOT NULL,\n" +
                    "    name VARCHAR(30) NOT NULL,\n" +
                    "    path VARCHAR(30) NOT NULL,\n" +
                    "    expiry BIGINT NOT NULL,\n" +
                    "    maxExpiry BIGINT NOT NULL,\n" +
//                    "    created BIGINT NOT NULL,\n" +
//                    "    lastUpdated BIGINT NOT NULL\n" +
                    ");\n");
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS \"cookie_uuid_uindex\" ON PUBLIC.cookie (uuid);");
        } catch (SQLException e){
            System.err.println("SQLException caught in CookieDatabase.createTable(): " + e);
            e.printStackTrace();
        } System.out.println("DB: Table 'PUBLIC.cookie' created in H2 database");
    }

    public void insertCookie(ProxyCookie cookie){
        // For Cookie variables expiry and maxExpiry, Date's getTime() is used to store millisecond values in the database as BIGINT
        String query = String.format("INSERT INTO PUBLIC.cookie (uuid, host, name, path, expiry, maxExpiry) " +
                        "VALUES ('%s','%s','%s','%s','%s','%s')", cookie.getUuid(), cookie.getHost(), cookie.getName(),
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

    public static void main(String[] args) {
        CookieDatabase db = new CookieDatabase();
        db.createTable();

        // Creating test entries
        db.insertCookie(new DefaultProxyCookie("test-cookie", "name", "/", "host.com", new Date(new Date().getTime() + 30 * 60 * 1000), new Date(new Date().getTime() + 120 * 60 * 1000), new HashMap<>(1)));
        for (int i=1; i<5; i++){
            db.insertCookie(new DefaultProxyCookie(UUID.randomUUID().toString(), "name"+i, "/", "host.com", new Date(new Date().getTime() + 30 * 60 * 1000), new Date(new Date().getTime() + 120 * 60 * 1000), new HashMap<>(1)));
        }
    }
}

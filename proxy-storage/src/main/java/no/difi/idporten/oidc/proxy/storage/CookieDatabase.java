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

    public static void main(String[] args) {
        CookieDatabase db = new CookieDatabase();
        db.createTable();
    }
}

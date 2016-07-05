package no.difi.idporten.oidc.proxy.storage;

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

    public CookieDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
            //server = Server.createTcpServer().start();
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
        } catch (SQLException |ClassNotFoundException sqlE){
            sqlE.printStackTrace();
        }
    }

    public void createTable(){
        System.out.println("CREATE TABLE");
        try{
            //statement.execute("DROP TABLE IF EXISTS PUBLIC.cookie");
            statement.execute("CREATE TABLE IF NOT EXISTS PUBLIC.cookie\n" +
                    "(\n" +
                    "    uuid VARCHAR(36) PRIMARY KEY NOT NULL,\n" +
                    "    userData BLOB,\n" +
                    "    host VARCHAR(30) NOT NULL,\n" +
                    "    expiry BIGINT NOT NULL,\n" +
                    "    maxExpiry BIGINT NOT NULL,\n" +
                    "    created BIGINT NOT NULL,\n" +
                    "    lastUpdated BIGINT NOT NULL\n" +
                    ");\n");
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS \"cookie_uuid_uindex\" ON PUBLIC.cookie (uuid);");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        CookieDatabase di = new CookieDatabase();
        di.createTable();
    }


}

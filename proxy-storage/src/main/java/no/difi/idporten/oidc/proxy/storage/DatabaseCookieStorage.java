package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.DefaultProxyCookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class DatabaseCookieStorage implements CookieStorage {

    private static Logger logger = LoggerFactory.getLogger(DatabaseCookieStorage.class);

    private CookieDatabase db;
    private static DatabaseCookieStorage ourInstance = new DatabaseCookieStorage();

    public static DatabaseCookieStorage getInstance() {
        return ourInstance;
    }

    private DatabaseCookieStorage() {
        System.out.println("\nDatabaseCookieStorage instantiated");
        db = new CookieDatabase();
        db.createTable();
    }

    /**
     * Invoked in InboundHandlerAdapter when the proxy server receives a JWT from the authorization server.
     * Creates a ProxyCookie object, inputs it in the database (through the CookieDatabase class),
     * and returns the object. The cookie is available for the user only if the time of the request
     * precedes created [Date] + maxExpiry [int] and lastUpdated [Date] + touchPeriod [int].
     *
     * @param cookieName  String (e.g. "google-cookie")
     * @param host        String (e.g. "www.google.com")
     * @param path        String (e.g. "/oauth")
     * @param touchPeriod int (in minutes)
     * @param userData    HashMap<String, String> (JWT from authorization server)
     * @return ProxyCookie (DefaultProxyCookie) object
     */
    @Override
    public ProxyCookie generateCookieInDb(String cookieName, String host, String path, int touchPeriod, int maxExpiry, HashMap<String, String> userData) {
        System.out.println("\nDatabaseCookieStorage.generateCookieInDb()\n");
        String uuid = UUID.randomUUID().toString();
        ProxyCookie proxyCookie = new DefaultProxyCookie(uuid, cookieName, host, path, touchPeriod, maxExpiry, userData);
        db.insertCookie(proxyCookie);
        return proxyCookie;
    }

    /**
     * Looks up cookie with given uuid in the database and stores it in an Optional<ProxyCookie>.
     * Returns an empty Optional if cookie is not found, expiry or maxExpiry is reached, or given
     * host or path doesn't match that of the cookie. Extends cookie expiry if cookie is valid.
     *
     * @param uuid String
     * @param host String
     * @param path String
     * @return Optional<ProxyCookie>
     */
    @Override
    public Optional<ProxyCookie> findCookie(String uuid, String host, String path) {
        Optional<ProxyCookie> result = db.findCookie(uuid);

        if (result.isPresent()) {
            // Check expiry and maxExpiry
            if (!result.get().isValid()) {
                System.err.println("Cookie was found, but has expired");
                return Optional.empty();
            }
            // Check host and path
            if (result.get().getHost().equals(host) && result.get().getPath().equals(path)) {
                System.out.println("\nCookie is valid (host & path matches):");
                CookieDatabase.printCookie(result.get());
                extendCookieExpiry(result.get().getUuid());
                // Cookie is returned with old expiry, but this doesn't matter, as no fixed expiry is set in browser
                return result;
                // Check host
            } else if (result.get().getHost().equals(host)) {
                System.out.println("\nCookie is valid (host matches):");
                CookieDatabase.printCookie(result.get());
                extendCookieExpiry(result.get().getUuid());
                // Cookie is returned with old expiry, but this doesn't matter, as no fixed expiry is set in browser
                return result;
            } else {
                // Used for debug at the moment. 'else' is otherwise redundant, as the same is returned further down
                System.err.println("Cookie is found, but host and/or path does not match:");
                CookieDatabase.printCookie(result.get());
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * When the user logs in again, the session's cookie expiry is expanded. Expands cookie expiry by
     * updating the lastUpdated variable in the cookie, to present time. Does not handle the instance
     * where new expiry exceeds max expiry here, both of these are validated before returning a found
     * cookie is returned.
     *
     * @param uuid String
     */
    private void extendCookieExpiry(String uuid) {
        db.extendCookieExpiry(uuid);
    }

    @Override
    public void removeExpiredCookies() {
        logger.info("Removing expired cookies");
        db.removeExpiredCookies();
    }

    // Debug
    public void printAllCookies() {
        db.getAllCookies().values().forEach(CookieDatabase::printCookie);
    }

    // Debug
    /*
    public static void main(String[] args) {
        /*
        HashMap<String, String> userData = CookieDatabase.stringToHashMap("{at_hash=notARealHash1337, aud=sometestpath.googleusercontent.com, dfd=123123123123123, email_not_verified=true, azp=sometestpath.apps.googleusercontent.com, dsds=accounts.google.com, exp=123123123, iat=234234234, email=gmail@gmail.com}");
        HashMap<String, String> userData2 = CookieDatabase.stringToHashMap("{at_hash=notARealHash1338, hkk=sometestpath.apps.googleusercontent.com, øøæ=123123123123123, email_might_verified=true, azp=sometestpath.apps.googleusercontent.com, dsds=accounts.google.com, exp=123123123, iat=345345345, email=gmail@gmail.com}");
        HashMap<String, String> userData3 = CookieDatabase.stringToHashMap("{at_hash=notARealHash1339, sds=sometestpath.apps.googleusercontent.com, dfd=123123123123123, email_slightly_verified=true, azp=sometestpath.apps.googleusercontent.com, dsds=accounts.google.com, exp=123123123, iat=456456456, email=gmail@gmail.com}");


        ProxyCookie cookie = ourInstance.generateCookieInDb("db-cookie", "google.com", "/oauth", 20, 100, userData);
        ProxyCookie cookie2 = ourInstance.generateCookieInDb("db-cookie", "google2.com", "/oauth", 20, 100, userData2);
        ProxyCookie cookie3 = ourInstance.generateCookieInDb("db-cookie", "google3.com", "/oauth", 20, 100, userData3);


        System.out.println("\nPRINT ALL COOKIES\n");
        ourInstance.printAllCookies();

        System.out.println("\nfinding cookie\n");

        Optional<ProxyCookie> cook = ourInstance.findCookie(cookie.getUuid(), "google.com", "/oauth");


        System.out.println("\n\nAFTER FINDING COOKIES: PRINT ALL");
        ourInstance.printAllCookies();

        ourInstance.removeExpiredCookies();

    }*/
}

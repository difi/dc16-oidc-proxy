package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.DefaultProxyCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseCookieStorage implements CookieStorage {

    private static Logger logger = LoggerFactory.getLogger(DatabaseCookieStorage.class);


    private static final int MINUTE = 60 * 1000;
    private static int initialValidPeriod = 30;
    private static int expandSessionPeriod = 30;
    private static int maxValidPeriod = 120;
    private static CookieDatabase db = new CookieDatabase();

    private static DatabaseCookieStorage ourInstance = new DatabaseCookieStorage();

    public static DatabaseCookieStorage getInstance() {
        return ourInstance;
    }

    private static HashMap<String, String> defaultUserData; // just used for testing purposes

    private Map<String, DefaultProxyCookie> storedCookies;

    /**
     * Generates a MD5 hash based on a random new UUID and the parameters
     *
     * @param host
     * @param cookieName
     * @return
     */
    private static String generateDatabaseKey(String cookieName, String host, String path) {
        String uuid = UUID.randomUUID().toString();
        // ProxyCookie (DefaultProxyCookie) initialized with userData = null for the time being
        db.insertCookie(new DefaultProxyCookie(uuid, cookieName, host, path, new Date(new Date().getTime() + initialValidPeriod * MINUTE), new Date(new Date().getTime() + maxValidPeriod * MINUTE), null));
        return DummyCookieStorage.hashCookieBrowserId(uuid, cookieName, host, path);
    }

    private DatabaseCookieStorage() {
        defaultUserData = new HashMap<String, String>();
        defaultUserData.put("pid", "08023549930");
        storedCookies = new HashMap<String, DefaultProxyCookie>();
    }



    @Override
    public DefaultProxyCookie generateCookieAsObject(String name, String host, String path, HashMap<String, String> userData) {
        logger.debug("Generating cookie object {}@{}{}", name, host, path);
        return null;
    }

    /**
     * When the user logs in again, the session's cookie 'expiry' is expanded, but only if the cookie's
     * 'expiry' is not reached. Expands 'expiry' with the amount of time in 'expandSessionPeriod', if
     * it does not surpass 'maxExpiry'. If cookie is still valid, but expanding 'expiry' will surpass
     * 'maxExpiry', 'expiry' is set to 'maxExpiry'.
     * @param cookie
     */
    private void extendCookieExpiry(ProxyCookie cookie) {
        /*if( cookie )
        Date dateNow = new Date();

        Date newExpiry = new Date(dateNow.getTime() + expandSessionPeriod * MINUTE);
        if (newExpiry.after(cookie.getMaxExpiry())) {
            ((DefaultProxyCookie) cookie).setExpiry(cookie.getMaxExpiry());
        } else {
            cookie.setExpiry(newExpiry);
        }*/
    }

    @Override
    public Optional<ProxyCookie> findCookie(String uuid, String host, String path) {
        Date dateNow = new Date();
        //DefaultProxyCookie result = storedCookies.get(uuid);
        Optional<ProxyCookie> result = db.findCookie(uuid);

        if (result.isPresent()){
            if (result.get().getHost().equals(host) && result.get().getPath().equals(path)){
                extendCookieExpiry(result.get());
                return result;
            } else {
                // Used for debug at the moment. else is otherwise redundant, as the same is returned further down
                System.err.println("Cookie is found, but host and/or path does not match");
                return Optional.empty();
            }
        }
        System.err.println("Cookie with this uuid was not found: "+uuid);
        return Optional.empty();
    /*

        if (uuid.equals("uuidForValidCookie") && host.equals("localhost:8080")) { // only used for testing
            return Optional.of(new DefaultProxyCookie(
                    uuid,
                    "PROXYCOOKIE",
                    host,
                    path,
                    new Date(dateNow.getTime() + 60 * 24 * MINUTE),
                    new Date(dateNow.getTime() + 60 * 24 * MINUTE),
                    defaultUserData));
        } else if (result != null && result.getHost().equals(host) && result.getPath().equals(path)) {

            return Optional.of(result);
        } else {
            return Optional.empty();
        }*/
    }

    @Override
    public void removeExpiredCookies() {
        logger.info("Removing expired cookies");
        db.removeExpiredCookies();
    }

    public static void main(String[] args) {
        db.createTable();

        // Creating test entries
        db.insertCookie(new DefaultProxyCookie("test-cookie", "name", "host.com", "/", new Date(new Date().getTime() + 30 * 60 * 1000), new Date(new Date().getTime() + 120 * 60 * 1000), new HashMap<>(1)));
        db.insertCookie(new DefaultProxyCookie("expired-cookie1", "name", "host.com", "/", new Date(new Date().getTime() - 30 * 60 * 1000), new Date(new Date().getTime() - 120 * 60 * 1000), new HashMap<>(1)));
        db.insertCookie(new DefaultProxyCookie("expired-cookie2", "name", "host.com", "/", new Date(new Date().getTime() - 50 * 60 * 1000), new Date(new Date().getTime() - 220 * 60 * 1000), new HashMap<>(1)));
        db.insertCookie(new DefaultProxyCookie("expires-now-cookie", "name", "host.com", "/", new Date(new Date().getTime()), new Date(new Date().getTime() - 220 * 60 * 1000), new HashMap<>(1)));
        for (int i=1; i<5; i++){
            db.insertCookie(new DefaultProxyCookie(UUID.randomUUID().toString(), "name"+i, "host.com", "/", new Date(new Date().getTime() + 30 * 60 * 1000), new Date(new Date().getTime() + 120 * 60 * 1000), new HashMap<>(1)));
        }

        HashMap<String, ProxyCookie> cookies = db.getAllCookies();
        System.out.println("\n\ngetAllCookies HashMap:\n" + cookies);
        cookies.values().forEach(CookieDatabase::printCookie);

        logger.debug("TESTLOGGER");

        db.removeExpiredCookies();
        HashMap<String, ProxyCookie> cookies2 = db.getAllCookies();
        System.out.println("\n\ngetAllCookies HashMap:\n" + cookies2);
        cookies2.values().forEach(CookieDatabase::printCookie);


        // Finding a test entry
        Optional<ProxyCookie> testCookie = db.findCookie("test-cookie");
        if (testCookie.isPresent()){
            CookieDatabase.printCookie(testCookie.get());
        }

    }


}

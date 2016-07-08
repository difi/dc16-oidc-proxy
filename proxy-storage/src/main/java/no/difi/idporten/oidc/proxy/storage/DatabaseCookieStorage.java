package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.api.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.DefaultProxyCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
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


    /**
     * Generates a MD5 hash based on a random new UUID and the parameters
     *
     * @param host
     * @param cookieName
     * @return
     */
    private String generateCookie(String cookieName, String host, String path) {
        String uuid = UUID.randomUUID().toString();

        // ProxyCookie (DefaultProxyCookie) initialized with userData = null for the time being
        db.insertCookie(new DefaultProxyCookie(uuid, cookieName, host, path, new Date(new Date().getTime() + initialValidPeriod * MINUTE), new Date(new Date().getTime() + maxValidPeriod * MINUTE), null));
        //return DummyCookieStorage.hashBrowserCookieId(uuid, cookieName, host, path);
        return uuid;
    }

    private DatabaseCookieStorage() {
        // Instantiate
        db.createTable();
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
        Date newExpiry = new Date(cookie.getExpiry().getTime() + expandSessionPeriod * MINUTE);
        if (newExpiry.after(cookie.getMaxExpiry())) {
            newExpiry = cookie.getMaxExpiry();
        }
        // As of now, lastUpdated is set to the time it is inserted into the database, in CookieDatabase.extendCookieExpiry()
        db.extendCookieExpiry(cookie.getUuid(), newExpiry);
    }

    @Override
    public Optional<ProxyCookie> findCookie(String uuid, String host, String path) {
        long presentTime= new Date().getTime();
        Optional<ProxyCookie> result = db.findCookie(uuid);

        if (result.isPresent()){
            // Check expiry
            if (result.get().getExpiry().getTime() < presentTime){
                System.err.println("Cookie was found, but expired");
                return Optional.empty();
            }
            // Check host and path
            if (result.get().getHost().equals(host) && result.get().getPath().equals(path)){
                extendCookieExpiry(result.get());
                // Cookie is returned with old expiry?
                return result;
            } else {
                // Used for debug at the moment. else is otherwise redundant, as the same is returned further down
                System.err.println("Cookie is found, but host and/or path does not match");
                return Optional.empty();
            }
        }
        System.err.println("Cookie with this uuid was not found: "+uuid);
        return Optional.empty();
    }


    public void printAllCookies(){
        db.getAllCookies().values().forEach(CookieDatabase::printCookie);
    }

    @Override
    public void removeExpiredCookies() {
        logger.info("Removing expired cookies");
        db.removeExpiredCookies();
    }

    public static void main(String[] args) {
        //ourInstance.hash();

        // Creating test entries
        /*
        db.insertCookie(new DefaultProxyCookie("test-cookie", "name", "host.com", "/", new Date(new Date().getTime() + initialValidPeriod * MINUTE), new Date(new Date().getTime() + maxValidPeriod * MINUTE), new HashMap<>(1)));
        db.insertCookie(new DefaultProxyCookie("expired-cookie1", "name", "host.com", "/", new Date(new Date().getTime() - 30 * 60 * 1000), new Date(new Date().getTime() - 120 * 60 * 1000), new HashMap<>(1)));
        db.insertCookie(new DefaultProxyCookie("expired-cookie2", "name", "host.com", "/", new Date(new Date().getTime() - 50 * 60 * 1000), new Date(new Date().getTime() - 220 * 60 * 1000), new HashMap<>(1)));
        db.insertCookie(new DefaultProxyCookie("expires-now-cookie", "name", "host.com", "/", new Date(new Date().getTime()), new Date(new Date().getTime() - 220 * 60 * 1000), new HashMap<>(1)));
        for (int i=1; i<5; i++){
            db.insertCookie(new DefaultProxyCookie(UUID.randomUUID().toString(), "name"+i, "host.com", "/", new Date(new Date().getTime() + 30 * 60 * 1000), new Date(new Date().getTime() + 120 * 60 * 1000), new HashMap<>(1)));
        }
        */

        String uuid = ourInstance.generateCookie("db-cookie", "google.com", "/oauth");
        //String uuid2 = generateCookie("db-cookie2", "idporten.no", "/");
        //String uuid3 = generateCookie("db-cookie2", "facebook.com", "/people");

        System.out.println("\nPRINT ALL COOKIES\n");
        ourInstance.printAllCookies();

        System.out.println("\nfinding cookie\n");

        Optional<ProxyCookie> cook = ourInstance.findCookie(uuid, "google.com", "/oauth");
        if (cook.isPresent()){
            System.out.println("\nprinting found cookie:");
            db.printCookie(cook.get());
        }

//        ourInstance.findCookie(uuid, "google.com", "/oauth2");
//        ourInstance.findCookie(uuid, "google.com", "/");

        System.out.println("\n\nAFTER FINDING COOKIES: PRINT ALL");
        ourInstance.printAllCookies();


        logger.debug("TESTLOGGER");

        /*db.removeExpiredCookies();
        HashMap<String, ProxyCookie> cookies2 = db.getAllCookies();
        System.out.println("\n\ngetAllCookies HashMap:\n" + cookies2);
        cookies2.values().forEach(CookieDatabase::printCookie);*/

        ourInstance.removeExpiredCookies();


        // Finding a test entry
        Optional<ProxyCookie> testCookie = db.findCookie("test-cookie");
        if (testCookie.isPresent()){
            CookieDatabase.printCookie(testCookie.get());
        }



        System.out.println("\n\nEXTENDING EXPIRY\n\n");

        /*
        db.printCookie(db.findCookie("test-cookie").get());
        Optional<ProxyCookie> pc = ourInstance.findCookie("test-cookie", "host.com", "/");
        if (pc.isPresent())
            ourInstance.extendCookieExpiry(pc.get());
        db.printCookie(db.findCookie("test-cookie").get());
        */
    }


}

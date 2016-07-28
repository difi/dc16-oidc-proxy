package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
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
        logger.debug("DatabaseCookieStorage instantiated");
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
    public ProxyCookie generateCookieInDb(String cookieName, String host, String path, int touchPeriod, int maxExpiry, Map<String, String> userData) {
        String uuid = UUID.randomUUID().toString();
        ProxyCookie proxyCookie = new DefaultProxyCookie(uuid, cookieName, host, path, touchPeriod, maxExpiry, userData);
        db.insertCookie(proxyCookie);
        logger.info("Cookie generated and inserted into the database ({})", proxyCookie);
        return proxyCookie;
    }

    /**
     * Used for debug and testing. Enables using other values self-assigned values to created and
     * lastUpdated, instead of time of instantiation.
     *
     * @param proxyCookie object
     * @return same proxyCookie object unchanged
     */
    public ProxyCookie generateCookieInDb(ProxyCookie proxyCookie) {
        db.insertCookie(proxyCookie);
        logger.info("Cookie generated and inserted into the database ({})", proxyCookie);
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

        // Check if cookie is found
        if (result.isPresent()) {
            // Check expiry and maxExpiry
            if (result.get().isValid()) {
                // Check host and path
                if (result.get().getHost().equals(host) && result.get().getPath().equals(path)) {
                    logger.info("Cookie is valid - host and path matches ({})", result.get());
                    //CookieDatabase.printCookie(result.get());
                    return Optional.of(extendCookieExpiry(result.get()));
                    // Check only host
                } else if (result.get().getHost().equals(host)) {
                    logger.info("Cookie is valid - host matches ({})", result.get());
                    //CookieDatabase.printCookie(result.get());
                    return Optional.of(extendCookieExpiry(result.get()));
                } else {
                    //CookieDatabase.printCookie(result.get());
                    logger.info("Cookie was found, but host does not match (" + uuid + "@" + host + path + ")");
                }
            } else {
                logger.info("Cookie was found, but has expired ({})", result.get());
            }
        } else {
            logger.info("Cookie was not found (" + uuid + "@" + host + path + ")");
        }
        return Optional.empty();
    }

    /**
     * When the user logs in again, the session's cookie expiry is expanded. Expands cookie expiry by
     * updating the lastUpdated variable in the cookie, to present time. Does not handle the instance
     * where new expiry exceeds max expiry here, both of these are validated before returning a found
     * cookie is returned.
     *
     * @param proxyCookie object
     */
    private ProxyCookie extendCookieExpiry(ProxyCookie proxyCookie) {
        Date lastUpdated = new Date();
        Date calculatedMaxExpiry = calculateDate(proxyCookie.getCreated(), proxyCookie.getMaxExpiry());
        // Somewhat similar validation is performed in DatabaseCookieStorage.findCookie() using ProxyCookie.isValid()
        if (lastUpdated.after(calculatedMaxExpiry)) {
            logger.warn("Cannot extend expiry of expired cookie ({})", proxyCookie);
            return proxyCookie;
        }
        ((DefaultProxyCookie) proxyCookie).setLastUpdated(lastUpdated);
        db.extendCookieExpiry(proxyCookie.getUuid(), lastUpdated);
        logger.info("Extended expiry of cookie ({})", proxyCookie);
        return proxyCookie;
    }

    @Override
    public void removeExpiredCookies() {
        db.removeExpiredCookies();
        logger.info("Removed expired cookies");
    }

    public void removeCookie(String uuid) {
        db.removeCookie(uuid);
        logger.info("Removed cookie if it existed (UUID: " + uuid + ")");
    }

    // Debug
    public void printAllCookies() {
        db.getAllCookies().values().forEach(CookieDatabase::printCookie);
    }

    private static Date calculateDate(Date date, int minutes) {
        return new Date(date.getTime() + minutes * 60 * 1000);
    }

}

package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DatabaseCookieStorage implements CookieStorage {

    private static Logger logger = LoggerFactory.getLogger(DatabaseCookieStorage.class);

    private CookieDatabase db;

    private static DatabaseCookieStorage ourInstance = new DatabaseCookieStorage();

    public static DatabaseCookieStorage getInstance() {
        return ourInstance;
    }

    private DatabaseCookieStorage() {
        db = new CookieDatabase();
    }

    /**
     * Invoked in InboundHandlerAdapter when the proxy server receives a JWT from the authorization server.
     * Creates a ProxyCookie object, inputs it in the database (through the CookieDatabase class),
     * and returns the object. The cookie is available for the user only if the time of the request
     * precedes created [Date] + maxExpiry [int] and lastUpdated [Date] + touchPeriod [int].
     *
     * @param uuid        String
     * @param cookieName  String (e.g. "google-cookie")
     * @param host        String (e.g. "www.google.com")
     * @param idp         String (e.g. "google")
     * @param touchPeriod int (in minutes)
     * @param userData    HashMap<String, String> (JWT from authorization server)
     * @return ProxyCookie (DefaultProxyCookie) object
     */
    public ProxyCookie generateCookieInDb(String uuid, String cookieName, String host, String idp, int security, int touchPeriod, int maxExpiry, Map<String, String> userData) {
        ProxyCookie proxyCookie = new DefaultProxyCookie(uuid, cookieName, host, idp, security, touchPeriod, maxExpiry, userData);
        return generateCookieInDb(proxyCookie);
    }

    public ProxyCookie generateCookieInDb(String cookieName, String host, String idp, int security, int touchPeriod, int maxExpiry, Map<String, String> userData) {
        return generateCookieInDb(UUID.randomUUID().toString(), cookieName, host, idp, security, touchPeriod, maxExpiry, userData);
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
        return proxyCookie;
    }

    /**
     * @param uuid
     * @param host
     * @param preferredIdpData
     * @return
     */
    @Override
    public Optional<ProxyCookie> findCookie(String uuid, String host, List<Map.Entry<String, String>> preferredIdpData) {
        Optional<List<ProxyCookie>> result = db.findCookies(uuid);

        if (result.isPresent() && !result.get().isEmpty()) {
            List<ProxyCookie> cookies = result.get().stream().filter(ProxyCookie::isValid).filter(pc -> pc.getHost().equals(host)).collect(Collectors.toList());

            if (!cookies.isEmpty()) {
                List<String> cookieIdps = cookies.stream().map(ProxyCookie::getIdp).collect(Collectors.toList());
                List<String> desiredIdps = preferredIdpData.stream().map(Map.Entry::getKey).collect(Collectors.toList());

                if (!Collections.disjoint(cookieIdps, desiredIdps)) {
                    ProxyCookie proxyCookie = null;
                    Map<String, String> userData = new HashMap<>();
                    boolean primaryUserDataFound = false;

                    for (Map.Entry<String, String> preferredIdp : preferredIdpData) {
                        if (cookieIdps.contains(preferredIdp.getKey()) && !primaryUserDataFound) {
                            proxyCookie = cookies.get(cookieIdps.indexOf(preferredIdp.getKey()));
                            if (proxyCookie.getUserData() != null) userData.putAll(proxyCookie.getUserData());
                            primaryUserDataFound = true;
                            extendCookieExpiry(proxyCookie);
                        } else if (cookieIdps.contains(preferredIdp.getKey()) && !userData.containsKey(preferredIdp.getKey())) {
                            extendCookieExpiry(cookies.get(cookieIdps.indexOf(preferredIdp.getKey())));
                            String additionalData = cookies.get(cookieIdps.indexOf(preferredIdp.getKey())).getUserData().get(preferredIdp.getValue());
                            if (additionalData != null) {
                                userData.put("X-additional-data/" + preferredIdp.getKey() + "-" + preferredIdp.getValue(), additionalData);
                            }
                        }
                    }

                    if (proxyCookie != null) {
                        ((DefaultProxyCookie) proxyCookie).setUserData(userData);
                        return Optional.of(proxyCookie);
                    }
                }
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
     * @param proxyCookie object
     */
    private ProxyCookie extendCookieExpiry(ProxyCookie proxyCookie) {
        Date now = new Date();
        Date touchExpiry = calculateDate(proxyCookie.getLastUpdated(), proxyCookie.getTouchPeriod());
        Date maxExpiry = calculateDate(proxyCookie.getCreated(), proxyCookie.getMaxExpiry());

        if (now.after(touchExpiry) || now.after(maxExpiry)) {
            logger.warn("Cannot extend expiry of expired cookie ({})", proxyCookie);
            return proxyCookie;
        }
        ((DefaultProxyCookie) proxyCookie).setLastUpdated(now);
        db.extendCookieExpiry(proxyCookie.getUuid(), proxyCookie.getIdp(), now);
        return proxyCookie;
    }

    @Override
    public void removeExpiredCookies() {
        db.removeExpiredCookies();
        logger.debug("Removed expired cookies");
    }

    @Override
    public void removeCookie(String uuid) {
        db.removeCookie(uuid);
    }

    private static Date calculateDate(Date date, int minutes) {
        return new Date(date.getTime() + minutes * 60 * 1000);
    }
}

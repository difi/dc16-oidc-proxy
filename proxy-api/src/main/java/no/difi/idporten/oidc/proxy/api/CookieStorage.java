package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.ProxyCookie;

import java.util.Map;
import java.util.Optional;

public interface CookieStorage {

    /**
     * Generates a cookie object, inputs it into a database, and returns the object.
     *
     * @param name        String (e.g. "google-cookie")
     * @param host        String (e.g. "www.google.com")
     * @param path        String (e.g. "/oauth")
     * @param touchPeriod int (amount of minutes)
     * @param maxExpiry   int (amount of minutes)
     * @param userData    HashMap<String, String> (JWT from authorization server)
     * @return ProxyCookie implemented object
     */
    ProxyCookie generateCookieInDb(String name, String host, String path, int security,
                                   int touchPeriod, int maxExpiry, Map<String, String> userData);

    /**
     * If cookie with given uuid exist in 'cookies' list; return object implementing ProxyCookie, otherwise return null.
     * Updates expiry of cookie, by setting lastUpdated to present time.
     */
    Optional<ProxyCookie> findCookie(String uuid, String host, String path);

    /**
     * Removes cookie with given UUID. Used primarily for logout functionality,
     * deleting the user's cookie in our database.
     *
     * @param uuid String
     */
    void removeCookie(String uuid);

    /**
     * Remove all expired cookies in the storage.
     */
    void removeExpiredCookies();

}

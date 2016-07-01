package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.DefaultProxyCookie;
import no.difi.idporten.oidc.proxy.model.UserData;

import java.util.HashMap;
import java.util.Optional;

public interface CookieStorage {

    /**
     * Creates a new DefaultProxyCookie object with these attributes; universally unique identifier (UUID) [String],
     * host [String], expiry [Date] and maxExpiry [Date]. Returns UUID as a string.
     */
    String generateCookie(String host, HashMap<String, String> userData);

    ProxyCookie generateCookieAsObject(String host, HashMap<String, String> userData);

    /**
     * When the user logs in again, the session's cookie 'expiry' is expanded, but only if the cookie's
     * 'expiry' is not reached. Expands 'expiry' with the amount of time in 'expandSessionPeriod', if
     * it does not surpass 'maxExpiry'. If cookie is still valid, but expanding 'expiry' will surpass
     * 'maxExpiry', 'expiry' is set to 'maxExpiry'.
     */
    void extendCookieExpiry(DefaultProxyCookie cookie);

    /**
     * Method for saving a new cookie or updating a current cookie depending on whether it already exists or not.
     * @param cookie
     * @return Optional.of(ProxyCookie) if successful, Optional.empty() otherwise.
     */
    Optional<ProxyCookie> saveOrUpdateCookie(ProxyCookie cookie);

    /**
     * If cookie with given uuid exist in 'cookies' list; return DefaultProxyCookie object, otherwise return null
     */
    Optional<ProxyCookie> findCookie(String uuid, String host);

    /**
     * Remove all expired cookies in 'cookies' list, by mutating the list.
     */
    void removeExpiredCookies();

}

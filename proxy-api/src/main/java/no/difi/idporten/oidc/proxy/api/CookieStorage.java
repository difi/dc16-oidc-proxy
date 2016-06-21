package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.Cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface CookieStorage {

    /**
     * Creates a new Cookie object with these attributes; universally unique identifier (UUID) [String],
     * host [String], expiry [Date] and maxExpiry [Date]. Returns UUID as a string.
     */
    String generateCookie(String host);

    /**
     * When the user logs in again, the session's cookie 'expiry' is expanded, but only if the cookie's
     * 'expiry' is not reached. Expands 'expiry' with the amount of time in 'expandSessionPeriod', if
     * it does not surpass 'maxExpiry'. If cookie is still valid, but expanding 'expiry' will surpass
     * 'maxExpiry', 'expiry' is set to 'maxExpiry'.
     */
    void extendCookieExpiry(Cookie cookie);

    /**
     * If cookie with given uuid exist in 'cookies' list; return Cookie object, otherwise return null
     */
    Optional<Cookie> findCookie(String uuid, String host);

    /**
     * Remove all expired cookies in 'cookies' list, by mutating the list.
     */
    void removeExpiredCookies();

}

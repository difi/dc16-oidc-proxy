package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.Cookie;

import java.util.ArrayList;
import java.util.List;

public interface CookieStorage {
    int MINUTE = 60*1000;         // Convert from milliseconds
    int initialValidPeriod = 30;  // The amount of time (in minutes) a session is initially validated for
    int maxValidPeriod = 120;     // The maximum amount of time (in minutes) a session is valid for
    int expandSessionPeriod = 15; // The amount of time (in minutes) expiry is expanded for, with every login within its valid period
    List<Cookie> cookies = new ArrayList<Cookie>(); // Contains all cookies created in the class implementing this interface

    // Creates a new Cookie object with these attributes; universally unique identifier (UUID) [String],
    // host [String], expiry [Date], maxExpiry [Date], created [Date] and lastUpdated [Date]. Returns UUID as a string.
    String generateCookie(String host);

    // When the user logs in again, the session's cookie 'expiry' is expanded, but only if the cookie's
    // 'expiry' is not reached. Expands 'expiry' with the amount of time in 'expandSessionPeriod', if
    // it does not surpass 'maxExpiry'. If cookie is still valid, but expanding 'expiry' will surpass
    // 'maxExpiry', 'expiry' is set to 'maxExpiry'.
    void expandCookieExpiry(String uuid);

    // Returns true if cookie has expired or if cookie doesn't exist (most likely because it was deleted after expiry)
    boolean hasExpired(String uuid);

    // If cookie with given uuid exist in 'cookies' list; return Cookie object, otherwise return null
    Cookie findCookie(String uuid);

    // Returns true if cookie with given uuid exist in 'cookies' list
    boolean containsCookie(String uuid);

    // Returns a list of all cookies not expired. Does not mutate 'cookies' list
    List<Cookie> getValidCookies();

    // Remove all expired cookies in 'cookies' list, by mutating the list
    void removeExpiredCookies();

}

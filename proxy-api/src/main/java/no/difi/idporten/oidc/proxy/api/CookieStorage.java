package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.DefaultProxyCookie;
import no.difi.idporten.oidc.proxy.model.UserData;

import java.util.HashMap;
import java.util.Optional;

public interface CookieStorage {

    /**
     * Generates a cookie and stores it in the database.
     *
     * @param host
     * @param userData
     * @return
     */
    ProxyCookie generateCookieAsObject(String name, String host, String path, HashMap<String, String> userData);

    /**
     * If cookie with given uuid exist in 'cookies' list; return DefaultProxyCookie object, otherwise return null
     * Updates expiry date of cookie.
     */
    Optional<ProxyCookie> findCookie(String uuid, String host, String path);

    /**
     * Remove all expired cookies in 'cookies' list, by mutating the list.
     */
    void removeExpiredCookies();

}

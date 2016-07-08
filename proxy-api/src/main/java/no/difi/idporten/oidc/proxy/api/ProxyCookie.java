package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.UserData;

import java.util.Date;
import java.util.HashMap;

/**
 * Interface for the cookie object we use.
 */
public interface ProxyCookie {
    /**
     * @return Whether the cookie is expired, but may be expanded to do other checks?
     */
    boolean isValid();

    /**
     * @return UserData object
     */
    HashMap<String, String> getUserData();

    String getUuid();

    String getName();

    String getHost();

    String getPath();

    Date getExpiry();

    Date getMaxExpiry();

    Date getLastUpdated();

    Date getCreated();
}

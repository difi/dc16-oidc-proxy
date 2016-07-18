package no.difi.idporten.oidc.proxy.api;

import java.util.Date;
import java.util.Map;

/**
 * Interface for the cookie object we use.
 */
public interface ProxyCookie {

    /**
     * @return Whether the cookie is expired, but may be expanded to do other checks?
     */
    boolean isValid();

    String getUuid();

    String getName();

    String getHost();

    String getPath();

    int getTouchPeriod();

    int getMaxExpiry();

    Map<String, String> getUserData();

    Date getCreated();

    Date getLastUpdated();
}

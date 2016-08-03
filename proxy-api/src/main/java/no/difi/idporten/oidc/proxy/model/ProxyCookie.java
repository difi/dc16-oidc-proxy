package no.difi.idporten.oidc.proxy.model;

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

    String getIdp();

    int getTouchPeriod();

    int getMaxExpiry();

    Map<String, String> getUserData();

    Date getCreated();

    Date getLastUpdated();

    int getSecurity();
}

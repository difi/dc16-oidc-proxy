package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.model.ProxyCookie;

import java.util.Date;
import java.util.Map;


public class DefaultProxyCookie implements ProxyCookie {

    private static final int MINUTE = 60 * 1000;

    private String uuid, host, idp, name;

    private Map<String, String> userData;

    private final int touchPeriod; // in minutes

    private final int maxExpiry;  // in minutes

    private int security;

    private final Date created;

    private Date lastUpdated;

    /**
     * Constructor used for instantiating an object, with created and lastUpdated set to present time.
     * Used in every case, except when an object is instantiated with values from the database.
     *
     * @param uuid String of a 128 bit, type 4 (pseudo randomly generated) Universally Unique ID
     * @param name String (e.g. "google-cookie")
     * @param host String (e.g. "www.google.com")
     * @param idp String (e.g. "/oauth")
     * @param touchPeriod int (amount of minutes)
     * @param maxExpiry int (amount of minutes)
     * @param userData HashMap<String, String> (JWT from authorization server)
     */
    public DefaultProxyCookie(String uuid, String name, String host, String idp, int security,
                              int touchPeriod, int maxExpiry, Map<String, String> userData) {
        this.uuid = uuid;
        this.name = name;
        this.host = host;
        this.idp = idp;
        this.security = security;
        this.touchPeriod = touchPeriod;
        this.maxExpiry = maxExpiry;
        this.userData = userData;
        this.created = new Date();
        this.lastUpdated = new Date();
    }

    /**
     * Only used to instantiate a cookie from the database. Also used for testing. Otherwise,
     * DatabaseCookieStorage won't be able to validate the cookie with correct created and lastUpdated
     * values. In every other case, use other constructor to create a cookie with created and
     * lastUpdated values set to time of instantiation.
     *
     * @param uuid String of a 128 bit, type 4 (pseudo randomly generated) Universally Unique ID
     * @param name String (e.g. "google-cookie")
     * @param host String (e.g. "www.google.com")
     * @param idp String (e.g. "/oauth")
     * @param touchPeriod int (amount of minutes)
     * @param maxExpiry int (amount of minutes)
     * @param userData HashMap<String, String> (JWT from authorization server)
     * @param lastUpdated Date (last time the cookie was "touched" (extended expiry))
     * @param created Date (time of creation)
     */
    public DefaultProxyCookie(String uuid, String name, String host, String idp, int security, int touchPeriod,
                              int maxExpiry, Map<String, String> userData, Date created, Date lastUpdated) {
        this.uuid = uuid;
        this.name = name;
        this.host = host;
        this.idp = idp;
        this.security = security;
        this.touchPeriod = touchPeriod;
        this.maxExpiry = maxExpiry;
        this.userData = userData;
        this.created = created;
        this.lastUpdated = lastUpdated;
    }

    /**
     * Checks if the cookie's expiry and maxExpiry is valid (not yet reached).
     *
     * @return Boolean (false if cookie has expired)
     */
    @Override
    public boolean isValid() {
        Date now = new Date();
        Date expiry = new Date(lastUpdated.getTime() + touchPeriod * MINUTE);
        Date maxExpiry = new Date(created.getTime() + getMaxExpiry() * MINUTE);
        return expiry.after(now) && maxExpiry.after(now);
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getIdp() {
        return idp;
    }

    @Override
    public int getTouchPeriod() {
        return touchPeriod;
    }

    @Override
    public int getMaxExpiry() {
        return maxExpiry;
    }

    @Override
    public Map<String, String> getUserData() {
        return userData;
    }

    @Override
    public Date getCreated() {
        return created;
    }



    public Date getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public int getSecurity() {
        return security;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return String.format("%s@%s-%s", uuid, host, idp);
    }
}

package no.difi.idporten.oidc.proxy.model;

import no.difi.idporten.oidc.proxy.api.ProxyCookie;

import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultProxyCookie implements ProxyCookie {

    private static Logger logger = LoggerFactory.getLogger(DefaultProxyCookie.class);

    private static final int MINUTE = 60 * 1000;
    private String uuid, host, path, name;
    private HashMap<String, String> userData;
    private Date lastUpdated = new Date();
    private final Date created = new Date();
    private int touchPeriod;     // in minutes
    private final int maxExpiry; // in minutes

    public DefaultProxyCookie(String uuid, String name, String host, String path, int touchPeriod, int maxExpiry, HashMap<String, String> userData) {
        System.err.println("\nDefaulProxyCookie constructor\n");
        this.userData = userData;
        this.uuid = uuid; // Universally unique identifier
        this.name = name;
        this.host = host; // Hostname (e.g. 'nav.no')
        this.path = path;
        this.touchPeriod = touchPeriod; // in minutes
        this.maxExpiry = maxExpiry;     // in minutes
    }



    @Override
    public boolean isValid() {
        System.err.println("\nDefaultProxyCookie.isValid()\n");
        //logger.debug("Checking if cookie is valid with expiry date: {}", expiry);
        Date now = new Date();
        Date expiry = new Date(lastUpdated.getTime() + touchPeriod * MINUTE);
        Date maxExpiry = new Date(created.getTime() + getMaxExpiry() * MINUTE);
        return expiry.after(now) && maxExpiry.after(now);
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public int getMaxExpiry() {
        return maxExpiry;
    }

    /*
    public void setExpiry(Date expiry) {
        if (expiry.after(getMaxExpiry())) {
            this.expiry = getMaxExpiry();
        } else {
            this.expiry = expiry;
        }
        touch();
    }
    */

    public Date getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getHost() {
        return host;
    }

    private void touch() {
        lastUpdated = new Date();
    }

    @Override
    public String toString() {
        return String.format("%s@%s%s", uuid, host, path);
    }

    @Override
    public HashMap<String, String> getUserData() {
        return userData;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public int getTouchPeriod() {
        return touchPeriod;
    }
}

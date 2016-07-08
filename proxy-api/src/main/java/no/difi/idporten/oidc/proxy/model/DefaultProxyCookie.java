package no.difi.idporten.oidc.proxy.model;

import no.difi.idporten.oidc.proxy.api.ProxyCookie;

import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultProxyCookie implements ProxyCookie {

    private static Logger logger = LoggerFactory.getLogger(DefaultProxyCookie.class);

    private String uuid, host, path, name;
    private HashMap<String, String> userData;
    private Date expiry;
    private Date lastUpdated = new Date();
    private final Date created = new Date();
    private final Date maxExpiry;

    public DefaultProxyCookie(String uuid, String name, String host, String path, Date expiry, Date maxExpiry, HashMap<String, String> userData) {
        this.userData = userData;
        this.uuid = uuid; // Universally unique identifier
        this.name = name;
        this.host = host; // Hostname (e.g. 'nav.no')
        this.path = path;
        this.expiry = expiry;
        this.maxExpiry = maxExpiry;
    }

    @Override
    public boolean isValid() {
        logger.debug("Checking if cookie is valid with expiry date: {}", expiry);
        return expiry.after(new Date());
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public Date getMaxExpiry() {
        return maxExpiry;
    }

    public void setExpiry(Date expiry) {
        if (expiry.after(getMaxExpiry())) {
            this.expiry = getMaxExpiry();
        } else {
            this.expiry = expiry;
        }
        touch();
    }

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

    @Override
    public Date getExpiry() {
        return expiry;
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
}

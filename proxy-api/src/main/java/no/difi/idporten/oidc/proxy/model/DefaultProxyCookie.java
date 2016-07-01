package no.difi.idporten.oidc.proxy.model;

import no.difi.idporten.oidc.proxy.api.ProxyCookie;

import java.util.Date;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultProxyCookie implements ProxyCookie {

    private static Logger logger  = LoggerFactory.getLogger(DefaultProxyCookie.class);

    private String uuid, host;
    private HashMap<String, String> userData;
    private Date expiry;
    private Date lastUpdated = new Date();
    private final Date created = new Date();
    private final Date maxExpiry;

    public DefaultProxyCookie(String uuid, String host, Date expiry, Date maxExpiry, HashMap<String, String> userData) {
        this.userData = userData;
        this.uuid = uuid; // Universally unique identifier
        this.host = host; // Hostname (e.g. 'nav.no')
        this.expiry = expiry;
        this.maxExpiry = maxExpiry;
    }

    @Override
    public boolean isValid(){
        logger.debug("Checking if cookie is valid with expiry date: {}", expiry);
        return expiry.after(new Date());
    }

    public Date getCreated() {
        return created;
    }

    public Date getMaxExpiry() {
        return maxExpiry;
    }

    public void setExpiry(Date expiry) {
        // If expiry.compareTo(maxExpiry) equals -1, expiry Date is before maxExpiry Date
        // If expiry.compareTo(maxExpiry) equals 0, expiry Date equals maxExpiry Date
        if (expiry.compareTo(maxExpiry) < 1){
            this.expiry = expiry;
            touch();
        } // Handle invalid 'expiry' ('expiry' after 'maxExpiry')?
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getUuid() {
        return uuid;
    }

    public String getHost() {
        return host;
    }

    public Date getExpiry() {
        return expiry;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void touch() {
        lastUpdated = new Date();
    }

    public String toString(){
        return String.format("%s@%s", uuid, host);
    }

    @Override
    public HashMap<String, String> getUserData() {
        return userData;
    }
}

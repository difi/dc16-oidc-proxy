package no.difi.idporten.oidc.proxy.model;

import java.util.Date;

public class Cookie {

    private String uuid, host;
    private Date expiry;
    private Date lastUpdated = new Date();
    private final Date created = new Date();
    private final Date maxExpiry;

    public Cookie(String uuid, String host, Date expiry, Date maxExpiry) {
        this.uuid = uuid; // Universally unique identifier
        this.host = host; // Hostname (e.g. 'nav.no')
        this.expiry = expiry;
        this.maxExpiry = maxExpiry;
    }

    public boolean isValid(){
        if (new Date().after(expiry)) return true;
        return false;
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
}

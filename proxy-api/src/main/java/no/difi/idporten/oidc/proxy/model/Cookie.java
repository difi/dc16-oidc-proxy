package no.difi.idporten.oidc.proxy.model;

import java.util.Date;

public class Cookie {

    private String uuid, host;
    private Date expiry, lastUpdated;
    private final Date created, maxExpiry;

    public Cookie(String uuid, String host, Date expiry, Date maxExpiry, Date created, Date lastUpdated) {
        this.uuid = uuid; // Universally unique identifier
        this.host = host; // Hostname (e.g. 'nav.no')
        this.expiry = expiry;
        this.maxExpiry = maxExpiry;
        this.created = created;
        this.lastUpdated = lastUpdated;
    }

    public boolean hasExpired(){
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
            lastUpdated = new Date(); // 'lastUpdated' is set to now
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

    public String toString(){
        return uuid + "@" + host;
    }

}

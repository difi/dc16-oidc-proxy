package no.difi.idporten.oidc.proxy.model;

public class AccessRequirement {

    private Host host;
    private int minLevel;

    public AccessRequirement(Host host, int minLevel){
        this.host = host;
        this.minLevel = minLevel;
    }
    public void setHost(Host host){
        this.host = host;
    }

    public void setMinLevel(int minLevel){
        this.minLevel = minLevel;
    }
}

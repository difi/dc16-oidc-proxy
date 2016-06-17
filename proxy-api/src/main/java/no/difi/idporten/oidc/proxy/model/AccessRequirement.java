package no.difi.idporten.oidc.proxy.model;

public class AccessRequirement {

    private Host host;
    private int minLevel;

    //Constructor, set host and minimum level of security
    public AccessRequirement(Host host, int minLevel){
        this.host = host;
        this.minLevel = minLevel;
    }

    //Setter for host
    public void setHost(Host host){
        this.host = host;
    }

    //Setter for min security level
    public void setMinLevel(int minLevel){
        this.minLevel = minLevel;
    }
}

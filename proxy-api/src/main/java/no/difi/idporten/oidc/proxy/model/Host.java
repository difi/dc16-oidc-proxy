package no.difi.idporten.oidc.proxy.model;

public class Host {

    //field hostName
    private String hostname;

    //Constructor, sets hostname
    public Host(String hostname){
        this.hostname = hostname;
    }

    //Setter for hostName
    public void setHostname(String hostname){
        this.hostname = hostname;
    }

}

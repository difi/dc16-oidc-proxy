package no.difi.idporten.oidc.proxy.model;

import java.util.ArrayList;
import java.util.List;

public class Host {

    //field hostName
    private List<String> hostname = new ArrayList<String>();
    private List<String> paths = new ArrayList<String>();
    private String idp;

    //Setter for hostName
    public void addHostname(String hostname) {
        this.hostname.add(hostname);
    }
    public void addPathname(String path) { this.paths.add(path);}
    public void setIdp(String idp) { this.idp = idp ;}

    public String getIdp() { return this.idp;}

    public List<String> getHostname(){
        return this.hostname;
    }
    public List<String> getPathnames() { return this.paths;}


    @Override
    public String toString() {
        return "Host{" +
                "hostname=" + hostname +
                '}';
    }
}

package no.difi.idporten.oidc.proxy.model;

import java.util.ArrayList;
import java.util.List;

public class Host {

    //field hostName
    private List<String> hostname = new ArrayList<String>();
    private List<Path> paths = new ArrayList<Path>();
    private String idp;
    private int minLevel;

    //Setter for hostName
    public void addHostname(String hostname) {
        this.hostname.add(hostname);
    }
    public void addPathname(Path path) { this.paths.add(path);}
    public void setIdp(String idp) { this.idp = idp ;}

    public String getIdp() { return this.idp;}

    public List<String> getHostname(){
        return this.hostname;
    }
    public List<Path> getPathnames() { return this.paths;}


    @Override
    public String toString() {
        return "Host{" +
                "hostname=" + hostname +
                '}';
    }
}

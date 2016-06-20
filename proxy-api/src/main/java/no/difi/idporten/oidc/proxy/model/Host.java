package no.difi.idporten.oidc.proxy.model;

import java.util.ArrayList;
import java.util.List;

public class Host {

    //field hostName
    private List<String> hostname = new ArrayList<String>();

    //Setter for hostName
    public void addHostname(String hostname) {
        this.hostname.add(hostname);
    }

    public List<String> getHostname(){
        return this.hostname;
    }


    @Override
    public String toString() {
        return "Host{" +
                "hostname=" + hostname +
                '}';
    }
}

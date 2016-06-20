package no.difi.idporten.oidc.proxy.model;

public class AccessRequirement {

    private Host host;
    private int minLevel;
    private Path path;

    //Constructor, set host and minimum level of security
    public AccessRequirement(Host host, int minLevel, Path path){
        this.host = host;
        this.minLevel = minLevel;
        this.path = path;
    }

    //Setter for host
    public void setHost(Host host){
        this.host = host;
    }

    //Setter for min security level
    public void setMinLevel(int minLevel){
        this.minLevel = minLevel;
    }
    public void setPath(Path path) { this.path = path;}
    public Host getHost(){
        return this.host;
    }

    public Path getPath() {
        return this.path;
    }

    public int getMinLevel(){
        return this.minLevel;
    }
}

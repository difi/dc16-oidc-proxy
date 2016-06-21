package no.difi.idporten.oidc.proxy.model;

public class AccessRequirement {

    private Host host;
    private int minLevel;
    private Path path;
    private String idp;

    //Constructor, set host and minimum level of security
    public AccessRequirement(Host host, int minLevel, Path path, String idp){
        this.host = host;
        this.minLevel = minLevel;
        this.path = path;
        this.idp = idp;
    }

    //Setters for the class variables
    public void setHost(Host host){
        this.host = host;
    }
    public void setIdp(String idp) { this.idp = idp;}
    public void setMinLevel(int minLevel){
        this.minLevel = minLevel;
    }
    public void setPath(Path path) { this.path = path;}

    //Getters
    public Host getHost(){
        return this.host;
    }
    public Path getPath() {
        return this.path;
    }
    public String getIdp(){
        return this.idp;
    }
    public int getMinLevel(){
        return this.minLevel;
    }
}

package no.difi.idporten.oidc.proxy.model;

public class Security {

    private int securityLevel;

    public void setSecurityLevel(int securityLevel){
        this.securityLevel = securityLevel;
    }

    public int getSecurityLevel(){
        return this.securityLevel;
    }

    @Override
    public String toString() {
        return "Host{" +
                "security=" + Integer.toString(securityLevel) +
                '}';
    }
}

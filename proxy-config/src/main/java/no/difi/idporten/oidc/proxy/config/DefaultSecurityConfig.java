package no.difi.idporten.oidc.proxy.config;

import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import no.difi.idporten.oidc.proxy.model.IdpConfig;
import no.difi.idporten.oidc.proxy.model.PathConfig;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;

public class DefaultSecurityConfig implements SecurityConfig {

    private String hostname, path;
    private final Optional<PathConfig> PATH;
    private final IdpConfig IDP;

    public DefaultSecurityConfig(String hostname, String path, HostConfigProvider hostConfigProvider, IdpConfigProvider idpConfigProvider) {
        this.path = path;
        this.hostname = hostname;
        this.PATH = hostConfigProvider.getByHostname(hostname).getPathFor(path);
        this.IDP = idpConfigProvider.getByIdentifier(getIdp());
    }



    public String getHostname() {
        return this.hostname;
    }

    public String getPath() {
        return this.path;
    }

    public String getSecurity() {
        if (PATH.get().getSecurity() != null){
            return PATH.get().getSecurity();
        }
        else{
            if (getParameters().keySet().contains("security")){
                return getParameters().get("security");
            }
        }
        return null;
    }

    public String getRedirect_uri() {
        if (PATH.get().getRedirect_uri() == null) {
            return IDP.getRedirect_uri();
        }
        return PATH.get().getRedirect_uri();
    }

    public String getScope() {
        if (PATH.get().getScope() == null) {
            return IDP.getScope();
        }
        return PATH.get().getScope();
    }

    public String getIdp() {
        return PATH.get().getIdp();
    }

    public String getIdpClass() {
        return IDP.getIdpClass();
    }

    public String getClient_id() {
        return IDP.getClient_Id();
    }

    public String getPassword() {
        return IDP.getPassword();
    }

    public Map<String, String> getParameters() {
        return IDP.getParameters();
    }


}

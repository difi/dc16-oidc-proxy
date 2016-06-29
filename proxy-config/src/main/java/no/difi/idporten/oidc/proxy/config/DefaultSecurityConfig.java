package no.difi.idporten.oidc.proxy.config;

import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import no.difi.idporten.oidc.proxy.model.IdpConfig;
import no.difi.idporten.oidc.proxy.model.PathConfig;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Optional;

public class DefaultSecurityConfig implements SecurityConfig {

    private String hostname, path;
    private final Optional<PathConfig> PATH;
    private final HostConfig HOST;
    private final IdpConfig IDP;

    public DefaultSecurityConfig(String hostname, String path, HostConfigProvider hostConfigProvider, IdpConfigProvider idpConfigProvider) {
        this.path = path;
        this.hostname = hostname;
        this.HOST = hostConfigProvider.getByHostname(hostname);
        this.PATH = hostConfigProvider.getByHostname(hostname).getPathFor(path);
        this.IDP = idpConfigProvider.getByIdentifier(getIdp());
    }

    public IdentityProvider createIdentityProvider() {
        try{
            return (IdentityProvider) Class.forName(IDP.getIdpClass()).getConstructor(SecurityConfig.class).newInstance(this);

        } catch (Exception e){
            System.out.println(e);
        }
        return null;
    }


    @Override
    public SocketAddress getBackend() {
        return this.HOST.getBackend();
    }

    @Override
    public String getHostname() {
        return this.hostname;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getIdp() {
        return PATH.get().getIdp();
    }

    @Override
    public String getIdpClass() {
        return IDP.getIdpClass();
    }

    @Override
    public String getClient_id() {
        return IDP.getClient_Id();
    }

    @Override
    public String getPassword() {
        return IDP.getPassword();
    }

    @Override
    public Map<String, String> getParameters() {
        return IDP.getParameters();
    }

    @Override
    public String getSecurity() {
        if (PATH.get().getSecurity() != null) {
            return PATH.get().getSecurity();
        } else {
            if (getParameters().keySet().contains("security")) {
                return getParameters().get("security");
            }
        }
        return null;
    }

    @Override
    public String getRedirect_uri() {
        if (PATH.get().getRedirect_uri() == null) {
            return IDP.getRedirect_uri();
        }
        return PATH.get().getRedirect_uri();
    }

    @Override
    public String getScope() {
        if (PATH.get().getScope() == null) {
            return IDP.getScope();
        }
        return PATH.get().getScope();
    }


}

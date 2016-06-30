package no.difi.idporten.oidc.proxy.config;

import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.model.*;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Optional;

public class DefaultSecurityConfig implements SecurityConfig {

    private String hostname, path;
    private final PathConfig PATH;
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
        try {
            return (IdentityProvider) Class.forName(IDP.getIdpClass()).getConstructor(SecurityConfig.class).newInstance(this);
        } catch (ClassNotFoundException exc) {
            exc.printStackTrace();
            return null;
        }  catch (Exception exc) { // so many possible exceptions for this
            exc.printStackTrace();
            return null;
        }
    }

    @Override
    public CookieConfig getCookieConfig() {
        return HOST.getCookieConfig();
    }

    @Override
    public SocketAddress getBackend() {
        return HOST.getBackend();
    }

    @Override
    public String getHostname() {
        return HOST.getHostname();
    }

    @Override
    public String getPath() {
        return PATH.getPath();
    }

    @Override
    public String getIdp() {
        return PATH.getIdp();
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
    public String getParameter(String key) {
        return IDP.getParameter(key).orElse("");
    }

    @Override
    public String getSecurity() {
        if (PATH.getSecurity() == null) {
            return getParameter("security");
        } else {
            return PATH.getSecurity();
        }
    }

    @Override
    public String getRedirect_uri() {
        if (PATH.getRedirect_uri() == null) {
            return IDP.getRedirect_uri();
        }
        return PATH.getRedirect_uri();
    }

    @Override
    public String getScope() {
        if (PATH.getScope() == null) {
            return IDP.getScope();
        }
        return PATH.getScope();
    }

    @Override
    public String toString() {
        return "DefaultSecurityConfig{" +
                "hostname='" + hostname + '\'' +
                ", path='" + path + '\'' +
                ", PATH=" + PATH +
                ", HOST=" + HOST +
                ", IDP=" + IDP +
                '}';
    }
}

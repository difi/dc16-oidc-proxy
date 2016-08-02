package no.difi.idporten.oidc.proxy.config;

import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.model.*;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class DefaultSecurityConfig implements SecurityConfig {

    private String hostname;

    private String path;

    private final PathConfig PATH;

    private final HostConfig HOST;

    private final IdpConfig IDP;

    public DefaultSecurityConfig(String hostname, String path, HostConfigProvider hostConfigProvider, IdpConfigProvider idpConfigProvider) {
        this.hostname = hostname;
        this.path = path;
        this.HOST = hostConfigProvider.getByHostname(hostname);
        this.PATH = hostConfigProvider.getByHostname(hostname).getPathFor(path);
        this.IDP = idpConfigProvider.getByIdentifier(getIdp());
    }

    public Optional<IdentityProvider> createIdentityProvider() {
        try {
            return Optional.of((IdentityProvider) Class.forName(IDP.getIdpClass()).getConstructor(SecurityConfig.class).newInstance(this));
        } catch (ClassNotFoundException exc) {
            exc.printStackTrace();
            return Optional.empty();
        } catch (Exception exc) {
            exc.printStackTrace();
            return Optional.empty();
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
        return PATH.getIdentityProvider();
    }

    @Override
    public String getIdpClass() {
        return IDP.getIdpClass();
    }

    @Override
    public String getClientId() {
        return IDP.getClientId();
    }

    @Override
    public String getPassword() {
        return IDP.getPassword();
    }

    @Override
    public String getSalt() {
        return HOST.getSalt();
    }

    @Override
    public String getPublicSignature() {
        return IDP.getPublicSignature();
    }

    @Override
    public List<String> getUserDataNames() {
        if (IDP != null) {
            return IDP.getUserDataNames();
        } else {
            return new LinkedList<>();
        }
    }

    @Override
    public List<String> getUnsecuredPaths() {
        return HOST.getUnsecuredPaths();
    }


    @Override
    public boolean isTotallyUnsecured(String path) {
        return HOST.isTotallyUnsecured(path);
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
    public String getRedirectUri() {
        if (PATH.getRedirectUri() == null) {
            return IDP.getRedirectUri();
        }
        return PATH.getRedirectUri();
    }

    @Override
    public String getLogoutRedirectUri() {
        return HOST.getLogoutRedirectUri();
    }

    @Override
    public String getLogoutPostUri() {
        return HOST.getLogoutPostUri();
    }

    @Override
    public String getScope() {
        if (PATH.getScope() == null) {
            return IDP.getScope();
        }
        return PATH.getScope();
    }

    @Override
    public boolean isSecured() {
        return !getSecurity().equals("0");
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

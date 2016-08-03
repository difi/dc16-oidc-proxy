package no.difi.idporten.oidc.proxy.config;

import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.model.*;

import java.net.SocketAddress;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultSecurityConfig implements SecurityConfig {

    private String hostname;

    private String path;

    private List<Map.Entry<String, String>> preferredIdpData;

    private final PathConfig PATH;

    private final HostConfig HOST;

    private final IdpConfig IDP;

    private List<String> defaultUserDataNames;

    public DefaultSecurityConfig(String hostname, String path, HostConfigProvider hostConfigProvider, IdpConfigProvider idpConfigProvider) {
        this.hostname = hostname;
        this.path = path;
        this.HOST = hostConfigProvider.getByHostname(hostname);
        this.PATH = hostConfigProvider.getByHostname(hostname).getPathFor(path);
        this.IDP = idpConfigProvider.getByIdentifier(getIdp());
        setPreferredIdpData(idpConfigProvider);
        setDefaultUserDataNames(idpConfigProvider);
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
    public List<String> getPreferredIdps() {
        return HOST.getPreferredIdps();
    }

    @Override
    public String getIdp() {
        return PATH.getIdentityProvider();
    }

    private void setPreferredIdpData(IdpConfigProvider idpConfigProvider) {
        preferredIdpData = new ArrayList<>();

        if (IDP != null && !getIdp().equals("notConfigured")) {
            preferredIdpData.add(new AbstractMap.SimpleEntry<>(IDP.getIdentifier(), IDP.getPassAlongData()));
        }

        preferredIdpData.addAll(
                getPreferredIdps()
                        .stream()
                        .filter(idp -> !idp.equals(getIdp()))
                        .map(idp -> new AbstractMap.SimpleEntry<>(idp, idpConfigProvider.getByIdentifier(idp).getPassAlongData()))
                        .collect(Collectors.toList()));
    }

    private void setDefaultUserDataNames(IdpConfigProvider idpConfigProvider) {
        this.defaultUserDataNames = new LinkedList<>();
        HOST.getPreferredIdps().stream()
                .forEach(idpId -> defaultUserDataNames
                        .addAll(idpConfigProvider.getByIdentifier(idpId).getUserDataNames()));
    }

    @Override
    public List<Map.Entry<String, String>> getPreferredIdpData() {
        return this.preferredIdpData;
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
    public List<String> getUserDataNames() {
        if (IDP == null) {
            return defaultUserDataNames;
        } else {
            return IDP.getUserDataNames();
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
    public int getSecurity() {
        if (PATH.getSecurity() == null) {
            return parseInt(getParameter("security"));
        }
        return parseInt(PATH.getSecurity());
    }

    public static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("NumberFormatException caught in DefaultSecurityConfig.parseInt() while parsing security string to int");
        }
        return -1;
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
        return getSecurity() != 0;
    }

    @Override
    public boolean isLogoutPath() {
        return this.path.endsWith(getLogoutPostUri());
    }

    @Override
    public String toString() {
        return "DefaultSecurityConfig{" +
                "hostname='" + hostname + "'" +
                ", path='" + path + "'" +
                ", PATH=" + PATH + "'" +
                ", HOST=" + HOST + "'" +
                ", IDP=" + IDP + "'" +
                ", preferredIdpData=" + preferredIdpData +
                "}";
    }
}

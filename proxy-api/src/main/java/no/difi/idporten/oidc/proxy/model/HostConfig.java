package no.difi.idporten.oidc.proxy.model;


import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

public interface HostConfig {

    String getHostname();

    PathConfig getPathFor(String path);

    InetSocketAddress getBackend();

    CookieConfig getCookieConfig();

    List<String> getUnsecuredPaths();

    String getSalt();

    boolean isTotallyUnsecured(String path);

    String getLogoutRedirectUri();

    String getLogoutPostUri();

    List<String> getPreferredIdps();

    Optional<String> getErrorPageUrl();
}

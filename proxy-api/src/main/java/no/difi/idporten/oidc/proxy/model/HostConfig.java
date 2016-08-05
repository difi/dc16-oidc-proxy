package no.difi.idporten.oidc.proxy.model;


import java.net.InetSocketAddress;
import java.util.List;

public interface HostConfig {

    String getHostname();

    PathConfig getPathFor(String path);

    InetSocketAddress getBackend();

    CookieConfig getCookieConfig();

    List<String> getUnsecuredPaths();

    String getSalt();

    String getLogoutHeader();

    boolean isTotallyUnsecured(String path);

    String getLogoutRedirectUri();

    String getLogoutPostUri();

    List<String> getPreferredIdps();
}

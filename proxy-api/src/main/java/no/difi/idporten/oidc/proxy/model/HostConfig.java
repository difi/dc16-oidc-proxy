package no.difi.idporten.oidc.proxy.model;


import java.net.InetSocketAddress;
import java.util.Optional;

public interface HostConfig {

    String getHostname(); //?

    Optional<PathConfig> getPathFor(String path);

    InetSocketAddress getBackend();
}

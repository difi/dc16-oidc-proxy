package no.difi.idporten.oidc.proxy.model;

import java.util.Optional;

public interface HostConfig {

    String getHostname();

    Optional<Path> getForPath(String path);

    String getBackend();
}

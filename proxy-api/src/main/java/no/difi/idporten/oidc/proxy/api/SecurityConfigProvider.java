package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.SecurityConfig;

import java.util.Optional;

public interface SecurityConfigProvider {
    Optional<SecurityConfig> getConfig(String hostname, String path);
}

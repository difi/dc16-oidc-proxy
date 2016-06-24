package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.SecurityConfig;

public interface SecurityConfigProvider {

    SecurityConfig getConfig(String hostname, String path);

}

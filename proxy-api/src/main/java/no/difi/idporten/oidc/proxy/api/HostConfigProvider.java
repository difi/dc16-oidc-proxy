package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.HostConfig;

public interface HostConfigProvider {

    HostConfig getByHostname(String hostname);
}

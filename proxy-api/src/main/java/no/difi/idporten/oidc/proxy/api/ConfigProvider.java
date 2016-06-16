package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.AccessRequirement;

import java.net.URI;

public interface ConfigProvider {

    AccessRequirement forUri(URI uri);

}

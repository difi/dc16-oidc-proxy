package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.AccessRequirement;

import java.net.URI;

@Deprecated
public interface ConfigProvider {

    //Return different access requirements based on URI
    AccessRequirement forUri(URI uri);

}

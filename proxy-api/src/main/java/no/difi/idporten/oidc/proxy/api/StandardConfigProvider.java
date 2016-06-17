package no.difi.idporten.oidc.proxy.api;

import no.difi.idporten.oidc.proxy.model.AccessRequirement;
import no.difi.idporten.oidc.proxy.model.Host;

import java.net.URI;

public class StandardConfigProvider implements ConfigProvider {

    public AccessRequirement forUri(URI uri){
        Host host = new Host(uri.getHost());
        String path = uri.getPath();

        AccessRequirement accessRequirement = new AccessRequirement(host,3);
        return accessRequirement;
    }
}
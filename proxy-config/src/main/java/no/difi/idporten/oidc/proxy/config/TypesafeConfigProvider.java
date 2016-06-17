package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import no.difi.idporten.oidc.proxy.api.ConfigProvider;
import no.difi.idporten.oidc.proxy.model.AccessRequirement;
import no.difi.idporten.oidc.proxy.model.Host;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypesafeConfigProvider implements ConfigProvider {

    private Config config;

    private List<Host> hosts = new ArrayList<>();
    private Map<String, Host> hostHostname = new HashMap<>();

    @Inject
    public TypesafeConfigProvider(Config config) {
        this.config = config;

        for (String key : config.getObject("host").keySet()) {
            // ConfigObject configObject = config.getObject(String.format("host.%s", key));

            Host host = new Host();
            config.getStringList(String.format("host.%s.hostname", key)).stream()
                    .peek(hostname -> hostHostname.put(hostname, host))
                    .forEach(host::addHostname);
            hosts.add(host);
        }
    }

    @Override
    public AccessRequirement forUri(URI uri) {
        return new AccessRequirement(hostHostname.get(uri.getHost()), 0);
    }
}

package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.model.HostConfig;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TypesafeHostConfigProvider implements HostConfigProvider {

    private Map<String, HostConfig> hosts;

    @Inject
    public TypesafeHostConfigProvider(Config config) {
        hosts = config.getObject("host").keySet().stream()
                .map(key -> config.getConfig(String.format("host.%s", key)))
                .map(c -> new TypesafeHostConfig(c, config))
                .collect(Collectors.toMap(HostConfig::getHostname, Function.identity()));
        System.out.println(hosts);
    }

    @Override
    public HostConfig getByHostname(String hostname) { //Returns hostConfig based on hostName
        return hosts.get(hostname);
    }

}



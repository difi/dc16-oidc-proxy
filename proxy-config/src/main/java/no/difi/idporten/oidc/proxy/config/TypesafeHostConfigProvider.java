package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import no.difi.idporten.oidc.proxy.model.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TypesafeHostConfigProvider implements HostConfigProvider {

    private static Logger logger = LoggerFactory.getLogger(TypesafeHostConfigProvider.class);

    private Map<String, HostConfig> hostConfigs = new HashMap<>();

    @Inject
    public TypesafeHostConfigProvider(Config config) {
        logger.debug("Initiating");
        logger.debug("Host config object:\n{}", config.getObject("host"));

        config.getObject("host").entrySet().stream()
                .forEach(entry -> {
                    logger.debug("Key: {}", entry.getKey());
                    logger.debug("Value: {}", entry.getValue());
                    logger.debug("Backends: {}", config.getList(String.format("host.%s.backends", entry.getKey())));
                    String hostName = config.getString(String.format("host.%s.hostname", entry.getKey()));
                    logger.debug("Hostname: {}", hostName);
                    List<String> backends = new ArrayList<>();
                    backends.addAll(config.getStringList(String.format("host.%s.backends", entry.getKey())));
                    logger.debug("Backends: {}", backends);
                    List<Path> paths = new LinkedList<>();
                    config.getObjectList(String.format("host.%s.paths", entry.getKey())).forEach(path -> {
                        Path newPath = new Path(path.get("path").unwrapped().toString());
                        paths.add(newPath);
                    });
                    paths.forEach(path -> logger.debug("Path: {}", path));

                    HostConfig newHostConfig = new HostConfig(backends, paths);

                    logger.debug("Adding new HostConfig to provider:\n{}", newHostConfig);
                    hostConfigs.put(config.getString(String.format("host.%s.hostname", entry.getKey())), newHostConfig);
        });
    }

    @Override
    public HostConfig getByHostname(String hostname) {
        return this.hostConfigs.get(hostname);
    }
}

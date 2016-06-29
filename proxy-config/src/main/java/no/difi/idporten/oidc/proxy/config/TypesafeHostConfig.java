package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import no.difi.idporten.oidc.proxy.model.PathConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TypesafeHostConfig implements HostConfig {

    private static final AtomicInteger backendIndex = new AtomicInteger();

    private static Logger logger = LoggerFactory.getLogger(TypesafeHostConfig.class);

    private String hostname;
    private List<InetSocketAddress> backends;
    private List<PathConfig> paths;


    public TypesafeHostConfig(Config hostConfig) {

        this.hostname = hostConfig.getString("hostname");

        this.backends = hostConfig.getStringList("backends").stream()
                .map(b -> b.contains(":") ? b : b + ":80")
                .map(b -> b.split(":", 2))
                .map(b -> new InetSocketAddress(b[0], Integer.parseInt(b[1])))
                .collect(Collectors.toList());

        this.paths = hostConfig.getConfigList("paths")
                .stream()
                .map(Path::new)
                .collect(Collectors.toList());

    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public Optional<PathConfig> getPathFor(String path) {
        logger.debug("Getting path object for {}{}", hostname, path);
        logger.debug("All paths: ({})\n{}", paths.size(), paths);
        return paths.stream()
                .filter(pathObject -> path.startsWith(pathObject.getPath()))
                .findFirst();
    }

    @Override
    public InetSocketAddress getBackend() {
        // Simple round-robin implementation.
        return backends.get(Math.abs(backendIndex.incrementAndGet() % backends.size()));
    }
}

package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import no.difi.idporten.oidc.proxy.model.Path;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TypesafeHostConfig implements HostConfig {

    private static final AtomicInteger backendIndex = new AtomicInteger(1);

    private String hostname;
    private List<String> backends;
    private List<Path> paths;

    public TypesafeHostConfig(Config hostConfig) {
        this.hostname = hostConfig.getString("hostname");
        this.backends = hostConfig.getStringList("backends");
        this.paths = hostConfig.getConfigList("paths").stream()
                .map(c -> c.getString("path"))
                .map(Path::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public Optional<Path> getForPath(String path) {
        return paths.stream()
                .filter(p -> path.startsWith(p.getPath()))
                .findFirst();
    }

    @Override
    public String getBackend() {
        // Simple round-robin implementation.
        return backends.get(backendIndex.incrementAndGet() % backends.size());
    }
}

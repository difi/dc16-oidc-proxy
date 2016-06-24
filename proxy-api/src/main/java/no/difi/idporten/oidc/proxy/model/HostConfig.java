package no.difi.idporten.oidc.proxy.model;

import java.util.List;
import java.util.Optional;

public class HostConfig {

    private List<String> backends;
    private List<Path> paths;

    public HostConfig(List<String> backends, List<Path> paths) {
        this.backends = backends;
        this.paths = paths;
    }

    public Optional<Path> getForPath(String path) {
        return paths.stream()
                .filter(p -> path.startsWith(p.getPath()))
                .findFirst();
    }

    public String getBackend() {
        // TODO round robin (or should it be in the SecurityConfig?)
        return backends.get(0);
    }
}

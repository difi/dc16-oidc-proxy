package no.difi.idporten.oidc.proxy.model;

import no.difi.idporten.oidc.proxy.api.HostConfigProvider;

import java.util.List;
import java.util.NoSuchElementException;

public class HostConfig {

    private String hostName;
    private List<String> backends;
    private List<Path> paths;

    public HostConfig(String hostName, List<String> backends, List<Path> paths) {
        this.hostName = hostName;
        this.backends = backends;
        this.paths = paths;
    }

    public boolean hasPath(String path) {
        return paths.stream()
                .filter(pathObject -> pathObject.getPath().equals(path))
                .findFirst()
                .isPresent();
    }

    public Path getPath(String path) {
        try {
            return paths.stream()
                    .filter(pathObject -> pathObject.getPath().equals(path))
                    .findFirst()
                    .get();
        } catch (NoSuchElementException exc) {
            exc.printStackTrace();
            return null;
        }
    }

    public String getBackend() {
        // TODO round robin (or should it be in the SecurityConfig?)
        return backends.get(0);
    }
}

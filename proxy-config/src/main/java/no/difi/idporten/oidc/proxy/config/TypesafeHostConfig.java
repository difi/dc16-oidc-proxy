package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import no.difi.idporten.oidc.proxy.model.PathConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TypesafeHostConfig implements HostConfig {

    private static final AtomicInteger backendIndex = new AtomicInteger();

    private static Logger logger = LoggerFactory.getLogger(TypesafeHostConfig.class);

    private String hostname;

    private String logoutPostUri;

    private String logoutRedirectUri;

    private List<InetSocketAddress> backends;

    private List<PathConfig> paths;

    private List<String> preferredIdps;

    private CookieConfig cookieConfig;

    private String salt;

    private List<String> unsecuredPaths;

    private String errorPageUrl;


    public TypesafeHostConfig(Config hostConfig, Config globalConfig) {
        this.hostname = hostConfig.getString("hostname");

        this.backends = hostConfig.getStringList("backends").stream()
                .map(b -> b.contains(":") ? b : b + ":80")
                .map(b -> b.split(":", 2))
                .map(b -> new InetSocketAddress(b[0], Integer.parseInt(b[1])))
                .collect(Collectors.toList());

        this.paths = hostConfig.getConfigList("paths")
                .stream()
                .map(TypesafePathConfig::new)
                .collect(Collectors.toList());

        this.preferredIdps = hostConfig.getStringList("preferred_idps");

        this.cookieConfig = new TypesafeCookieConfig(hostConfig.withFallback(globalConfig).getConfig("cookie"));

        this.salt = (hostConfig.withFallback(globalConfig).getString("salt"));

        this.unsecuredPaths = hostConfig.getStringList("unsecured_paths")
                .stream()
                .collect(Collectors.toList());

        this.logoutPostUri = hostConfig.getString("logout_post_uri");

        this.logoutRedirectUri = hostConfig.getString("logout_redirect_uri");

        setErrorPageUrl(hostConfig);

    }

    /**
     * Sets the error page url and handles both default path and invalid url syntax.
     *
     * @param hostConfig
     */
    private void setErrorPageUrl(Config hostConfig) {
        if (hostConfig.hasPath("error_page_uri")) {
            try {
                this.errorPageUrl = new URI(hostConfig.getString("error_page_uri")).toString();
            } catch (URISyntaxException exc) {
                logger.warn("Could not read error page uri: {}", hostConfig.getString("error_page_uri"));
                this.errorPageUrl = null;
            }
        } else {
            this.errorPageUrl = null;
        }
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public PathConfig getPathFor(String path) {
        return paths.stream()
                .filter(pathObject -> path.startsWith(pathObject.getPath()))
                .findFirst()
                .orElse(TypesafePathConfig.getUnsecuredPath());
    }

    @Override
    public InetSocketAddress getBackend() {
        // Simple round-robin implementation.
        return backends.get(Math.abs(backendIndex.incrementAndGet() % backends.size()));
    }

    @Override
    public CookieConfig getCookieConfig() {
        return this.cookieConfig;
    }

    @Override
    public List<String> getUnsecuredPaths() {
        return this.unsecuredPaths;
    }

    @Override
    public String getSalt() {
        return this.salt;
    }

    @Override
    public boolean isTotallyUnsecured(String path) {
        return unsecuredPaths.stream().filter(path::startsWith).findFirst().isPresent();
    }

    @Override
    public String getLogoutRedirectUri() {
        return this.logoutRedirectUri;
    }

    @Override
    public String getLogoutPostUri() {
        return this.logoutPostUri;
    }

    @Override
    public List<String> getPreferredIdps() {
        return this.preferredIdps;
    }

    public Optional<String> getErrorPageUrl() {
        if (this.errorPageUrl == null) {
            return Optional.empty();
        } else {
            return Optional.of(this.errorPageUrl);
        }
    }
}

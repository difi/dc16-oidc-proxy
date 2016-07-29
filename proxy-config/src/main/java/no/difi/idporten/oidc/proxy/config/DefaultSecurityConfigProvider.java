package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSecurityConfigProvider implements SecurityConfigProvider {

    private HostConfigProvider hostConfigProvider;

    private IdpConfigProvider idpConfigProvider;

    private Map<String, Optional<SecurityConfig>> memoizationCache;

    @Inject
    public DefaultSecurityConfigProvider(HostConfigProvider hostConfigProvider, IdpConfigProvider idpConfigProvider) {
        this.hostConfigProvider = hostConfigProvider;
        this.idpConfigProvider = idpConfigProvider;
        this.memoizationCache = new ConcurrentHashMap<>();
    }


    @Override
    public Optional<SecurityConfig> getConfig(String hostname, String path) {
        return memoizationCache.computeIfAbsent(hostname + path, this::privateGetConfig);
    }

    private Optional<SecurityConfig> privateGetConfig(String url) {
        String hostname = url.substring(0, url.indexOf('/'));
        String path = url.substring(url.indexOf('/'), url.length());
        if (hostConfigProvider.getByHostname(hostname) == null) {
            return Optional.empty();
        } else {
            return Optional.of(new DefaultSecurityConfig(hostname, path, hostConfigProvider, idpConfigProvider));
        }
    }
}

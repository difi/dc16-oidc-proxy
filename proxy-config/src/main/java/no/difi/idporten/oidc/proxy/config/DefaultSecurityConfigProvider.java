package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.model.HostConfig;
import no.difi.idporten.oidc.proxy.model.IdpConfig;
import no.difi.idporten.oidc.proxy.model.PathConfig;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class DefaultSecurityConfigProvider implements SecurityConfigProvider {

    Logger logger = LoggerFactory.getLogger(DefaultSecurityConfigProvider.class);

    private HostConfigProvider hostConfigProvider;
    private IdpConfigProvider idpConfigProvider;

    @Inject
    public DefaultSecurityConfigProvider(HostConfigProvider hostConfigProvider, IdpConfigProvider idpConfigProvider) {
        this.hostConfigProvider = hostConfigProvider;
        this.idpConfigProvider = idpConfigProvider;
    }


    @Override
    public Optional<SecurityConfig> getConfig(String hostname, String path) {
        logger.debug("Getting config for {}{}", hostname, path);
        HostConfig hostConfig = hostConfigProvider.getByHostname(hostname);
        if (hostConfig == null)
            return Optional.empty();
        Optional<PathConfig> pathOptional = hostConfig.getPathFor(path);
        IdpConfig idpConfig;
        if (pathOptional.isPresent()) {
            idpConfig = idpConfigProvider.getByIdentifier(pathOptional.get().getIdp());
            return Optional.of(new TypesafeSecurityConfig(idpConfig, hostConfig));
        } else {
            return Optional.empty();
        }
    }
}

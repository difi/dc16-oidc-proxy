package no.difi.idporten.oidc.proxy.config;

import com.google.inject.AbstractModule;
import no.difi.idporten.oidc.proxy.api.ConfigProvider;

/**
 * Guice module for configuration.
 */
public class ConfigModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ConfigProvider.class).to(StandardConfigProvider.class);
        
    }
}

package no.difi.idporten.oidc.proxy.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import no.difi.idporten.oidc.proxy.api.ConfigProvider;
import no.difi.idporten.oidc.proxy.api.HostConfigProvider;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;

/**
 * Guice module for configuration.
 */
public class ConfigModule extends AbstractModule {

    private String identifier;

    public ConfigModule() {
        this("application");
    }

    public ConfigModule(String identifier) {
        this.identifier = identifier;
    }

    @Override
    protected void configure() {
        bind(Config.class).toInstance(ConfigFactory.load(identifier));
        bind(ConfigProvider.class).to(TypesafeConfigProvider.class).in(Singleton.class);
        bind(HostConfigProvider.class).to(TypesafeHostConfigProvider.class).in(Singleton.class);
        bind(IdpConfigProvider.class).to(TypesafeIdpConfigProvider.class).in(Singleton.class);
        bind(SecurityConfigProvider.class).to(DefaultSecurityConfigProvider.class).in(Singleton.class);
    }
}

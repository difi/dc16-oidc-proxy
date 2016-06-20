package no.difi.idporten.oidc.proxy.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import no.difi.idporten.oidc.proxy.api.ConfigProvider;

/**
 * Guice module for configuration.
 */
public class ConfigModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Config.class).toInstance(ConfigFactory.load());
        bind(ConfigProvider.class).to(TypesafeConfigProvider.class).in(Singleton.class);
        
    }
}

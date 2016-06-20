package no.difi.idporten.oidc.proxy.storage;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import no.difi.idporten.oidc.proxy.api.CookieStorage;

public class StorageModule extends AbstractModule {

    @Override
    protected void configure() {
        // No action.
    }

    @Provides
    public CookieStorage cookieStorageProvider() {
        return new CookieHandler();
    }
}

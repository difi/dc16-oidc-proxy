package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import no.difi.idporten.oidc.proxy.storage.DummyCookieStorage;

public class TypesafeCookieConfig implements CookieConfig {

    private String name;

    public TypesafeCookieConfig(Config config) {
        this.name = config.getString("name");
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public CookieStorage getCookieStorage() {
        return DummyCookieStorage.getInstance();
    }
}

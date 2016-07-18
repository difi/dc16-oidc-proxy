package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import no.difi.idporten.oidc.proxy.storage.DatabaseCookieStorage;

public class TypesafeCookieConfig implements CookieConfig {

    private String name;

    private int touchPeriod;
    private int maxExpiry;

    public TypesafeCookieConfig(Config config){
        this.name = config.getString("name");
        this.touchPeriod = config.getInt("touchPeriod");
        this.maxExpiry = config.getInt("maxExpiry");
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getMaxExpiry() {
        return maxExpiry;
    }

    @Override
    public int getTouchPeriod() {
        return touchPeriod;
    }

    @Override
    public CookieStorage getCookieStorage() {
        return DatabaseCookieStorage.getInstance();
    }
}

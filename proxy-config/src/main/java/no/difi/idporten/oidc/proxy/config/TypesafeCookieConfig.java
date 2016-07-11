package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.model.CookieConfig;
import no.difi.idporten.oidc.proxy.storage.DatabaseCookieStorage;
import no.difi.idporten.oidc.proxy.storage.DummyCookieStorage;

public class TypesafeCookieConfig implements CookieConfig {

    private String name;
    private int touch;
    private int maxExpiry;

    public TypesafeCookieConfig(Config config){
        this.name = config.getString("name");
        this.touch = config.getInt("touch");
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
    public int getTouch() {
        return touch;
    }

    @Override
    /*public CookieStorage getCookieStorage() {
        return DummyCookieStorage.getInstance();
    }*/

    public CookieStorage getCookieStorage() {
        return DatabaseCookieStorage.getInstance();
    }
}

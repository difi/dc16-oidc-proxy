package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.model.CookieConfig;

public class TypesafeCookieConfig implements CookieConfig {

    private String name;

    public TypesafeCookieConfig(Config config){
        this.name = config.getString("name");
    }

    @Override
    public String getName() {
        return this.name;
    }

}

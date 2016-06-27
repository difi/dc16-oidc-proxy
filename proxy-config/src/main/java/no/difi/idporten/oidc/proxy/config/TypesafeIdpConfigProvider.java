package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.model.IdpConfig;

import java.util.*;

public class TypesafeIdpConfigProvider implements IdpConfigProvider {
    private Config idpConfig;
    private final String CONFIG = "idp";

    @Inject
    public TypesafeIdpConfigProvider(Config config) {
        idpConfig = config;

    }


    @Override
    public IdpConfig getByIdentifier(String identifier) {
        if (!idpConfig.isEmpty()){
            System.out.println(idpConfig.getConfig(CONFIG).getObject(identifier).getClass());

        }
        return null;
    }

    public static void main(String[] args) {
        new TypesafeIdpConfigProvider(ConfigFactory.load()).getByIdentifier("idporten");
    }


}

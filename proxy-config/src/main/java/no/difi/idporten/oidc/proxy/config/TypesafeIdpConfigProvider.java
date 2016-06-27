package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.model.AccessRequirement;
import no.difi.idporten.oidc.proxy.model.IdpConfig;

import java.util.ArrayList;
import java.util.List;

public class TypesafeIdpConfigProvider implements IdpConfigProvider {
    Config idpConfig;

    @Inject
    public TypesafeIdpConfigProvider(Config config) {
        idpConfig = config;


        // TODO Initiate cache.
    }

    public void run(){

        for (String key : idpConfig.getObject("idp").keySet()) {
            System.out.println(idpConfig.getStringList("idp"));
            //List<String> idpFields =  idpConfig.getStringList(String.format("idp.%s", key));

        }

    }

    @Override
    public IdpConfig getByIdentifier(String identifier) {
        // TODO Implement this.
        return null;
    }

    public static void main(String[] args) {
        new TypesafeIdpConfigProvider(ConfigFactory.load()).run();
    }


}

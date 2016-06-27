package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.model.AccessRequirement;
import no.difi.idporten.oidc.proxy.model.IdpConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypesafeIdpConfigProvider implements IdpConfigProvider {
    Config idpConfig;

    @Inject
    public TypesafeIdpConfigProvider(Config config) {
        idpConfig = config;


        // TODO Initiate cache.
    }

    public void run(){
        System.out.println(idpConfig.getStringList("idp.idporten"));
//        for (HashMap.Entry<String, Object> s:idpConfig.getObject("idp").unwrapped()){
//
//        }

//        System.out.println(idpConfig.getObject("idp"));
//
//        for (String key : idpConfig.getObject("idp").keySet()) {
//            System.out.println(idpConfig.getObject("idp"));
//            //List<String> idpFields =  idpConfig.getStringList(String.format("idp.%s", key));
//
//        }

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

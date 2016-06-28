package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.model.IdpConfig;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class TypesafeIdpConfigProvider implements IdpConfigProvider {
    private Map<String, IdpConfig> idps;

    @Inject
    public TypesafeIdpConfigProvider(Config config) {
        idps = config.getObject("idp").keySet().stream()
                .map(key -> config.getConfig(String.format("idp.%s", key)))
                .map(TypesafeIdpConfig::new)
                .collect(Collectors.toMap(IdpConfig::getIdentifier, Function.identity()));
        }

    @Override
    public IdpConfig getByIdentifier(String identifier) {
        return idps.get(identifier);
    }



}

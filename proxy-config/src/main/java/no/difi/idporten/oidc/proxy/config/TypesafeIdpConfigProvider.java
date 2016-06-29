package no.difi.idporten.oidc.proxy.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import no.difi.idporten.oidc.proxy.api.IdpConfigProvider;
import no.difi.idporten.oidc.proxy.model.IdpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class TypesafeIdpConfigProvider implements IdpConfigProvider {

    private static Logger logger = LoggerFactory.getLogger(TypesafeIdpConfigProvider.class);
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
        logger.debug("Getting idp by identifier: {}", identifier);
        logger.debug("Map of idps:\n{}", idps);
        return idps.get(identifier);
    }





}

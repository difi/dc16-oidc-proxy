package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.model.IdpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TypesafeIdpConfig implements IdpConfig {

    private static Logger logger = LoggerFactory.getLogger(TypesafeIdpConfig.class);


    private String identifier;

    private String idpClass;

    private String client_id;

    private String password;

    private String scope;

    private String redirect_uri;

    private List<String> user_data_names;

    private Map<String, String> parameters;

    public TypesafeIdpConfig(String identifier, Config idpConfig) {
        this.identifier = identifier;
        this.idpClass = idpConfig.getString("class");
        this.client_id = idpConfig.getString("client_id");
        this.password = idpConfig.getString("password");
        this.scope = idpConfig.getString("scope");
        this.redirect_uri = idpConfig.getString("redirect_uri");
        this.user_data_names = idpConfig.getStringList("user_data_name");
        this.parameters = idpConfig.getConfig("parameters").entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().unwrapped().toString()));

        logger.debug("Created IdpConfig:\n{}", this);
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getIdpClass() {
        return this.idpClass;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getScope() {
        return this.scope;
    }

    @Override
    public String getRedirectUri() {
        return this.redirect_uri;
    }

    @Override
    public String getClientId() {
        return this.client_id;
    }

    @Override
    public List<String> getUserDataNames() {
        return this.user_data_names;
    }

    @Override
    public Optional<String> getParameter(String key) {
        return parameters.containsKey(key) ? Optional.of(parameters.get(key)) : Optional.empty();
    }

    @Override
    public String toString() {
        return "TypesafeIdpConfig{" +
                "identifier='" + identifier + '\'' +
                ", idpClass='" + idpClass + '\'' +
                ", client_id='" + client_id + '\'' +
                ", password='" + password + '\'' +
                ", scope='" + scope + '\'' +
                ", redirect_uri='" + redirect_uri + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}

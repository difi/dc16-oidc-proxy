package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.idp.IdportenIdentityProvider;
import no.difi.idporten.oidc.proxy.model.IdpConfig;


import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TypesafeIdpConfig implements IdpConfig {

    private String identifier;
    private String idpclass;
    private String client_id;
    private String password;
    private String scope;
    private String redirect_uri;
    private Map<String, String> parameters;

    private static IdentityProvider getIdpByIdentifier(String identifier) {
        switch (identifier) {
            case "idporten":
                return new IdportenIdentityProvider();
            default:
                // return default idp
                return null;
        }
    }

    public TypesafeIdpConfig(Config idpConfig) {
        this.identifier = idpConfig.getString("identifier");
        this.idpclass = idpConfig.getString("class");
        this.client_id = idpConfig.getString("client_id");
        this.password = idpConfig.getString("password");
        this.scope = idpConfig.getString("scope");
        this.redirect_uri = idpConfig.getString("redirect_uri");
        /*
        this.parameters = idpConfig.getObjectList("parameters").stream()
                .collect(Collectors.toMap(
                        e -> e.unwrapped().keySet().toString().replaceAll("[\\[\\]]", ""),
                        p -> p.unwrapped().entrySet().toString().split("=")[1].replace("]", "")));
        */
    }

    @Override
    public IdentityProvider getIdp() {
        return getIdpByIdentifier(this.identifier);
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getIdpClass() {
        return this.idpclass;
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
    public String getRedirect_Uri() {
        return this.redirect_uri;
    }

    @Override
    public String getClient_Id() {
        return this.client_id;
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public String getValueFromParametersWithKey(String key) {
        return parameters.get(key);
    }
}

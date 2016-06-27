package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.model.IdpConfig;


import java.util.List;
import java.util.stream.Collectors;

public class TypesafeIdpConfig implements IdpConfig {

    private String identifier;
    private String idpclass;
    private String client_id;
    private String password;
    private String scope;
    private String redirect_uri;
    private List<String> parameters;

    public TypesafeIdpConfig(Config idpConfig){
        this.identifier = idpConfig.getString("identifier");
        this.idpclass = idpConfig.getString("class");
        this.client_id = idpConfig.getString("client_id");
        this.password = idpConfig.getString("password");
        this.scope = idpConfig.getString("scope");
        this.redirect_uri = idpConfig.getString("redirect_uri");
        this.parameters = idpConfig.getConfigList("parameters").stream()
                .map(c -> c.getString("quality"))
                .collect(Collectors.toList());
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
    public String getRedriect_Uri() {
        return this.redirect_uri;
    }

    @Override
    public String getClient_Id() {
        return this.client_id;
    }

    @Override
    public List<String> getParameters() {
        return this.parameters;
    }
}

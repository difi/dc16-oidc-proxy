package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.model.PathConfig;

public class TypesafePathConfig implements PathConfig {

    private String path, security, redirect_uri, scope, idp;

    public TypesafePathConfig(Config config) {
        this.path = config.getString("path");
        this.idp = config.getString("idp");

        if (checkForStringInConfig("security", config)) {
            this.security = config.getString("security");
        }

        if (checkForStringInConfig("redirect_uri", config)) {
            this.redirect_uri = config.getString("redirect_uri");
        }

        if (checkForStringInConfig("scope", config)){
            this.scope = config.getString("scope");
        }


    }

    private boolean checkForStringInConfig(String stringToBeChecked, Config config) {
        return (config.entrySet().toString().contains(stringToBeChecked));
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getIdp() {
        return this.idp;
    }

    @Override
    public String getSecurity() {
        return this.security;
    }

    @Override
    public String getRedirect_uri() {
        return this.redirect_uri;
    }

    @Override
    public String getScope() {
        return this.scope;
    }

    @Override
    public String toString(){
        return "path: " + path + ","
                +"security: " + security
                +", "
                +"redirect_uri: "+redirect_uri
                +", "
                +"scope: "+ scope
                +", "
                +"idp: "+idp;
    }


}

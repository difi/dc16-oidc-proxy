package no.difi.idporten.oidc.proxy.config;

import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.model.PathConfig;

public class TypesafePathConfig implements PathConfig {

    private String path, redirect_uri, scope, idp;

    private int security;

    private TypesafePathConfig(String path, int security, String idp) {
        this.path = path;
        this.security = security;
        this.idp = idp;
    }

    public TypesafePathConfig(Config config) {
        this.path = config.getString("path");
        this.idp = config.getString("idp");

        if (checkForStringInConfig("security", config)) {
            this.security = config.getInt("security");
        }

        if (checkForStringInConfig("redirect_uri", config)) {
            this.redirect_uri = config.getString("redirect_uri");
        }

        if (checkForStringInConfig("scope", config)) {
            this.scope = config.getString("scope");
        }

    }

    public static PathConfig getUnsecuredPath() {
        return new TypesafePathConfig("/", 0, "none");
    }

    private boolean checkForStringInConfig(String stringToBeChecked, Config config) {
        return (config.entrySet().toString().contains(stringToBeChecked));
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getIdentityProvider() {
        if (path.equals("notConfigured")) {
            return null;
        }
        return this.idp;
    }

    @Override
    public int getSecurity() {
        return this.security;
    }

    @Override
    public String getRedirectUri() {
        return this.redirect_uri;
    }

    @Override
    public String getScope() {
        return this.scope;
    }

    @Override
    public String toString() {
        return getPath();
    }


}

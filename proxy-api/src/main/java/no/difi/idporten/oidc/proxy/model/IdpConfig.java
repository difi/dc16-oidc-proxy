package no.difi.idporten.oidc.proxy.model;

import java.util.HashMap;

public class IdpConfig {

    private String idpclass;
    private String client_id;
    private String password;
    private String scope;
    private String redirect_uri;
    private HashMap<String, String> parameters;

    public IdpConfig(String idpclass, String client_id, String password, String scope, String redirect_uri, HashMap<String, String> parameters){
        this.idpclass=idpclass;
        this.client_id=client_id;
        this.password=password;
        this.scope=scope;
        this.redirect_uri=redirect_uri;
        this.parameters = parameters;
    }


};

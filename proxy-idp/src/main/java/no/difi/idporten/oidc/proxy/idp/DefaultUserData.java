package no.difi.idporten.oidc.proxy.idp;

import no.difi.idporten.oidc.proxy.model.UserData;

import java.util.HashMap;
import java.util.Map;

class DefaultUserData implements UserData {

    private Map<String, String> userData = new HashMap<>();

    public DefaultUserData(String id_token) {
        id_token = id_token.replace("\"", "").replace("{", "").replace("}", "");
        String[] tokens = id_token.split(",");
        for (String params : tokens) {
            String[] param = params.split(":");
            userData.put(param[0], param[1]);
        }
    }

    @Override
    public Map<String, String> getUserData() {
        return userData;
    }
}

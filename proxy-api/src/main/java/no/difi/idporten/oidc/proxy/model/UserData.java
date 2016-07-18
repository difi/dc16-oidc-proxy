package no.difi.idporten.oidc.proxy.model;

import java.util.HashMap;
import java.util.Map;

public class UserData {

    private Map<String, String> userData = new HashMap<>();

    public UserData(String id_token) {
        id_token = id_token.replace("\"", "").replace("{", "").replace("}", "");
        String[] tokens = id_token.split(",");
        for (String params : tokens) {
            String[] param = params.split(":");
            userData.put(param[0], param[1]);
        }
    }

    public Map<String, String> getUserData() {
        return userData;
    }
}

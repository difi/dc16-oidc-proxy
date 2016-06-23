package no.difi.idporten.oidc.proxy.model;

import java.util.HashMap;

public class UserData {


    private HashMap<String, String> userData;
    
    public UserData(String token){
        userData = new HashMap<>();
        token = token.replace("\"", "").replace("{", "").replace("}", "");
        String[] tokens = token.split(",");
        for (String params: tokens) {
            String[] param = params.split(":");
            userData.put(param[0], param[1]);
        }
    }

    public HashMap<String, String> getUserData() {
        return userData;
    }

}

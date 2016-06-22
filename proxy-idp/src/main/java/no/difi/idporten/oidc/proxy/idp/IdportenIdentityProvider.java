package no.difi.idporten.oidc.proxy.idp;

import com.sun.xml.internal.bind.v2.TODO;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;

import java.net.URI;
import java.util.Map;
import java.util.Scanner;

public class IdportenIdentityProvider implements IdentityProvider {

    //ClientID å endres til dificamp
    //

    private final String url = "https://eid-exttest.difi.no/opensso/oauth2/authorize?client_id=dificamp&scope=openid&response_type=code&redirect_uri=http://localhost:8080/";



    public URI generateURI(){
        URI uri = URI.create(url);
        return uri;
    }

    public Map<String, String> getToken(URI uri){
        //TODO: Get token with the given URI.
        return null;
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.println(new IdportenIdentityProvider().generateURI());
    }



}

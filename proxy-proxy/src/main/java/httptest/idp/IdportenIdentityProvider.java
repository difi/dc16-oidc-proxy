package httptest.idp;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;

import java.net.URI;
import java.util.Map;
import java.util.Scanner;

public class IdportenIdentityProvider implements IdentityProvider {


    public IdportenUrl generateURI(){
        IdportenUrl url = new IdportenUrl("https://eid-exttest.difi.no/opensso/oauth2/authorize");
        url.setScope("openid");
        url.setClient_id("dificamp");
        url.setResponse_type("code");
        url.setRedirect_uri("http://localhost:8080/");
        return url;
    }

    public String getToken(URI uri){
        String fukc = "6cf7e03a-77fc-4a0a-94dc-6628a7c18d24";
        String s = "https://eid-exttest.difi.no/idporten-oidc-provider/token" +
                "?grant_type=authorization_code&redirect_uri=localhost%3A8080%2F" +
                "dificamp%2Fauthorize%2Fresponse&code=";

        //TODO: Get token with the given URI.
        HttpRequest r = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST,s+fukc );

        return "";/*s+uri.toString().split("=|&|\\?")[2];*/

    }

    public static void main(String[] args) {
        new IdportenIdentityProvider().getToken(URI.create("fukboi.com"));
    }





}

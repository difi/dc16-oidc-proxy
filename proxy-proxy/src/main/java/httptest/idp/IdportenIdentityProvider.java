package httptest.idp;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
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



    public String getToken(URI uri) throws Exception{
        String s = "https://eid-exttest.difi.no/opensso/oauth2/access_token";
        String d = "grant_type=authorization_code&redirect_uri=http%3A%2F%2localhost%3A%2Fdificamp%2Fauthorize%2Fresponse&code=";
        String a = uri.toString().split("=|&|\\)")[1];
        String urlParameters = d+a;

        URL url = new URL(s);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Authorization", "Basic ZGlmaWNhbXA6cGFzc3dvcmQ=");




        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);
        System.out.println("Response message : " + con.getResponseMessage());

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        System.out.println(in);
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());


        return null;

    }

    public static void main(String[] args) throws Exception {
        new IdportenIdentityProvider().getToken(URI.create("fukboi.com"));
    }





}

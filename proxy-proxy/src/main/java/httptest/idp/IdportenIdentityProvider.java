package httptest.idp;

import com.google.api.client.json.webtoken.JsonWebToken;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.sun.javafx.tools.packager.Param;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.Map;


public class IdportenIdentityProvider implements IdentityProvider {



    //Uses the IdportenUrl class to generate a url.
    public IdportenUrl generateURI(){
        IdportenUrl url = new IdportenUrl("https://eid-exttest.difi.no/opensso/oauth2/authorize");
        url.setScope("openid");
        url.setClient_id("dificamp");
        url.setResponse_type("code");
        url.setRedirect_uri("http://localhost:8080/");
        return url;
    }

    /**Get token using the code from the log in at IDporten
    * @Param uri
    */
    public String getToken(String uri) throws Exception{
        //The base-url used to make a POST request
        String baseURL = "https://eid-exttest.difi.no/opensso/oauth2/access_token";
        //Parameteres used in the POST request
        String parameters = "grant_type=authorization_code&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2F&code=";
        //Code from the URI
        String code = uri.split("=|&|\\)")[1];
        System.out.println(code);

        String urlParameters = parameters+code;

        URL url = new URL(baseURL);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Authorization", "Basic " + Base64.getUrlEncoder().encodeToString("dificamp:password".getBytes()));

        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + con.getResponseCode());
        System.out.println("Response message : " + con.getResponseMessage());

        InputStream connectionIn;
        if (con.getResponseCode() == 200){
            connectionIn = con.getInputStream();
        }else{
            connectionIn = con.getErrorStream();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connectionIn));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        String[] tokens = response.toString().split(",");
        String id_token;
        for (String token: tokens) {
            if (token.contains("id_token")){
                id_token = decodeIDToken(token.split(":")[1].replace("\"", ""));
            }
        }

        return response.toString();

    }

    public String decodeIDToken(String id_token)throws Exception{
        id_token = JWTParser.parse(id_token).getJWTClaimsSet().toString();
        for (String param : id_token.split(",")) {
            System.out.println(param);
        }
        return id_token;

    }

}

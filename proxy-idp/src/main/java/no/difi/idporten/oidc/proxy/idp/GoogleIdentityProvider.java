package no.difi.idporten.oidc.proxy.idp;

import com.google.gson.JsonObject;
import com.nimbusds.jwt.JWTParser;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import no.difi.idporten.oidc.proxy.model.UserData;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class GoogleIdentityProvider extends AbstractIdentityProvider {

    private final SecurityConfig securityConfig;
    private final String APIURL = "https://www.googleapis.com/oauth2/v3/token"; // Google's api services
    private final String LOGINURL = "https://accounts.google.com/o/oauth2/auth"; // For logging in/authorizing with Google

    // Currently just something Viktor made on his Google account
    // Rather using config now
    private static String DIFICLIENTID = "1063910224877-dhqd36c09sitf9alq3jb0rfsfmebe35o.apps.googleusercontent.com";
    private static String DIFICLIENTSECRET = "MiUsgGqAUFPVoqjIDifJS-Rj";

    public GoogleIdentityProvider(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    @Override
    public String generateURI() throws IdentityProviderException {
        try {
            return new URIBuilder(LOGINURL)
                    .addParameter("scope", securityConfig.getScope())
                    .addParameter("client_id", securityConfig.getClient_id())
                    .addParameter("response_type", securityConfig.getParameter("response_type"))
                    .addParameter("access_type", securityConfig.getParameter("access_type"))
                    .addParameter("approval_prompt", securityConfig.getParameter("approval_prompt"))
                    .addParameter("redirect_uri", securityConfig.getRedirect_uri())
                    .build().toString();
        } catch (URISyntaxException e) {
            throw new IdentityProviderException(e.getMessage(), e);
        }
    }

    /**
     * Get token using the code from the log in at accounts.google.com
     */
    @Override
    public UserData getToken(String uri) throws IdentityProviderException {
        try {
            // The base-APIURL used to make a POST request
            String baseURL = APIURL;
            String code = uri.split("\\?code=")[1];

            HttpPost postRequest = new HttpPost(baseURL);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("code", code));
            params.add(new BasicNameValuePair("redirect_uri", securityConfig.getRedirect_uri())); // orElse("http://localhost:8080/google")
            params.add(new BasicNameValuePair("client_id", securityConfig.getClient_id()));
            params.add(new BasicNameValuePair("client_secret", securityConfig.getPassword()));
            params.add(new BasicNameValuePair("scope", securityConfig.getScope())); // orElse("")
            params.add(new BasicNameValuePair("grant_type", securityConfig.getParameter("grant_type")));
            postRequest.setEntity(new UrlEncodedFormEntity(params));

            System.out.println(String.format("Created post request:\n%s\n%s\n%s", postRequest, postRequest.getAllHeaders(), postRequest.getEntity()));


            HttpResponse httpResponse = httpClient.execute(postRequest);

            System.out.println("Sending 'POST' request to URL : " + baseURL);
            System.out.println("Post parameters : " + params);
            System.out.println("Response Code : " + httpResponse.getStatusLine().getStatusCode());
            System.out.println("Response message : " + httpResponse.getStatusLine().getReasonPhrase());
            // Must use complicated stream to read response and make it a json object
            InputStream in = httpResponse.getEntity().getContent();
            JsonObject jsonResponse;
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(in))) {
                jsonResponse = gson.fromJson(buffer, JsonObject.class);
                System.out.println("Response body as json :\n" + jsonResponse);
                return new UserData(decodeIDToken(jsonResponse.get("id_token").getAsString()));
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        } catch (Exception e) {
            throw new IdentityProviderException(e.getMessage(), e);
        }
        return null;
    }

    private String decodeIDToken(String id_token) throws Exception {
        return JWTParser.parse(id_token).getJWTClaimsSet().toString().replace("\\", "");
    }
}

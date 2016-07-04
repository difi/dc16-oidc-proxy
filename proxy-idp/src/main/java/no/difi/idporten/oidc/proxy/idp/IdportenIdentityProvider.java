package no.difi.idporten.oidc.proxy.idp;

import com.google.gson.JsonObject;
import com.nimbusds.jwt.JWTParser;
import com.typesafe.config.ConfigFactory;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import no.difi.idporten.oidc.proxy.model.UserData;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class IdportenIdentityProvider extends AbstractIdentityProvider {

    private SecurityConfig securityConfig;
    private String url = "https://eid-exttest.difi.no";

    public IdportenIdentityProvider(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    @Override
    public String generateURI() throws IdentityProviderException {
        try {
            // return new URIBuilder(url + "/opensso/oauth2/authorize")
            return new URIBuilder(url + "/idporten-oidc-provider/authorize")
                    .addParameter("scope", securityConfig.getScope())
                    .addParameter("client_id", securityConfig.getClient_id())
                    .addParameter("response_type", "code")
                    .addParameter("redirect_uri", securityConfig.getRedirect_uri())
                    .build().toString();
        } catch (URISyntaxException e) {
            throw new IdentityProviderException(e.getMessage(), e);
        }
    }

    /**
     * Get token using the code from the log in at ID-porten
     */
    @Override
    public UserData getToken(String uri) throws IdentityProviderException {
        try {
            // The base-url used to make a POST request
            // String baseURL = url + "/opensso/oauth2/access_token";
            String baseURL = url + "/idporten-oidc-provider/token";

            // Parsing parameters in provided uri
            Map<String, String> urlParameters = URLEncodedUtils.parse(URI.create(uri), "UTF-8").stream()
                    .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

            // Create content to be posted
            List<NameValuePair> contentValues = new ArrayList<NameValuePair>() {{
                add(new BasicNameValuePair("grant_type", "authorization_code"));
                add(new BasicNameValuePair("redirect_uri", securityConfig.getRedirect_uri()));
                add(new BasicNameValuePair("code", urlParameters.get("code")));
            }};
            String postContent = URLEncodedUtils.format(contentValues, StandardCharsets.UTF_8);

            // Initiate connection
            HttpPost httpPost = new HttpPost(baseURL);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setHeader("Authorization", "Basic " + Base64.getUrlEncoder().encodeToString((securityConfig.getClient_id() + ":" + securityConfig.getPassword()).getBytes()));
            httpPost.setEntity(new StringEntity(postContent));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            System.out.println("\nSending 'POST' request to URL : " + baseURL);
            System.out.println("Post parameters : " + postContent);
            System.out.println("Response Code : " + httpResponse.getStatusLine().getStatusCode());
            System.out.println("Response message : " + httpResponse.getStatusLine().getReasonPhrase());

            // Parse response
            JsonObject response;
            try (InputStream inputStream = httpResponse.getEntity().getContent()) {
                response = gson.fromJson(new InputStreamReader(inputStream), JsonObject.class);
            }

            return new UserData(decodeIDToken(response.get("id_token").getAsString()));
        } catch (Exception e) {
            throw new IdentityProviderException(e.getMessage(), e);
        }
    }

    private String decodeIDToken(String id_token) throws Exception {
        return JWTParser.parse(id_token).getJWTClaimsSet().toString().replace("\\", "");
    }

}

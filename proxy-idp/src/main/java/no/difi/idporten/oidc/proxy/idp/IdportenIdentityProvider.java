package no.difi.idporten.oidc.proxy.idp;

import com.google.gson.JsonObject;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import no.difi.idporten.oidc.proxy.model.UserData;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
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

    private HttpClient httpClient;

    private static String APIURL = "https://eid-exttest.difi.no/idporten-oidc-provider/token";

    private static String LOGINURL = "https://eid-exttest.difi.no/idporten-oidc-provider/authorize";

    private SecurityConfig securityConfig;

    public IdportenIdentityProvider(SecurityConfig securityConfig) {
        this.httpClient = HttpClientBuilder.create().build();
        this.securityConfig = securityConfig;
    }

    /**
     * Generates a redirect URI to IDPorten's login based on the current SecurityConfig
     *
     * @return uri
     * @throws IdentityProviderException
     */
    @Override
    public String generateRedirectURI() throws IdentityProviderException {
        try {
            return new URIBuilder(LOGINURL)
                    .addParameter("scope", securityConfig.getScope())
                    .addParameter("client_id", securityConfig.getClientId())
                    .addParameter("response_type", "code")
                    .addParameter("redirect_uri", securityConfig.getRedirectUri())
                    .build().toString();
        } catch (URISyntaxException e) {
            throw new IdentityProviderException(e.getMessage(), e);
        }
    }

    /**
     * Uses a code from when a user has authorized IDPorten to get some information about him to make a request
     * to the IDPorten API.
     *
     * @param uri containing a code and maybe some more information about the request.
     * @return DefaultUserData object containing information about the user.
     * @throws IdentityProviderException
     */
    @Override
    public UserData getToken(String uri) throws IdentityProviderException {
        try {
            Map<String, String> urlParameters = URLEncodedUtils.parse(URI.create(uri), "UTF-8").stream()
                    .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

            List<NameValuePair> contentValues = new ArrayList<>();
            contentValues.add(new BasicNameValuePair("grant_type", securityConfig.getParameter("grant_type")));
            contentValues.add(new BasicNameValuePair("redirect_uri", securityConfig.getRedirectUri()));
            contentValues.add(new BasicNameValuePair("code", urlParameters.get("code")));
            String postContent = URLEncodedUtils.format(contentValues, StandardCharsets.UTF_8);

            HttpPost httpPost = new HttpPost(APIURL);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getUrlEncoder().encodeToString(
                    (securityConfig.getClientId() + ":" + securityConfig.getPassword()).getBytes()));
            httpPost.setEntity(new StringEntity(postContent));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IdentityProviderException("Bad response from IdentityProvider API");
            }

            JsonObject jsonResponse;
            try (InputStream inputStream = httpResponse.getEntity().getContent()) {
                jsonResponse = gson.fromJson(new InputStreamReader(inputStream), JsonObject.class);
                if (securityConfig.getJSONWebKeys() != null) {
                    return new DefaultUserData(decodeIDToken(jsonResponse.get("id_token").getAsString(), securityConfig), jsonResponse.get("access_token").getAsString());
                }
                throw new IdentityProviderException("Configuration of this IDP is wrong");
            } catch (IOException exc) {
                throw new IdentityProviderException(exc.getMessage(), exc);
            }
        } catch (Exception exc) {
            throw new IdentityProviderException(exc.getMessage(), exc);
        }
    }
}

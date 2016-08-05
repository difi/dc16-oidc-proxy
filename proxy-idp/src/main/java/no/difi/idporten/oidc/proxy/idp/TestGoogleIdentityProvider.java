package no.difi.idporten.oidc.proxy.idp;

import com.google.gson.JsonObject;
import com.nimbusds.jwt.SignedJWT;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import no.difi.idporten.oidc.proxy.model.UserData;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestGoogleIdentityProvider extends AbstractIdentityProvider {

    private static Logger logger = LoggerFactory.getLogger(GoogleIdentityProvider.class);

    private HttpClient httpClient;

    private final SecurityConfig securityConfig;

    private List<String> redirectExtraParameters;

    private List<String> tokenExtraParameters;

    private static String APIURL = "http://localhost:8081/oauth2/v3/token";

    private static String LOGINURL = "http://localhost:8081/o/oauth2/auth";

    public TestGoogleIdentityProvider(SecurityConfig securityConfig) {
        this.httpClient = HttpClientBuilder.create().build();
        this.securityConfig = securityConfig;

        redirectExtraParameters = new LinkedList<>();
        redirectExtraParameters.add("response_type");
        redirectExtraParameters.add("access_type");
        redirectExtraParameters.add("approval_prompt");


        tokenExtraParameters = new LinkedList<>();
        tokenExtraParameters.add("grant_type");
    }

    /**
     * Generates a redirect URI to Google's login based on the current SecurityConfig
     *
     * @return uri
     * @throws IdentityProviderException
     */
    @Override
    public String generateRedirectURI() throws IdentityProviderException {
        try {
            URIBuilder uriBuilder = new URIBuilder(LOGINURL)
                    .addParameter("scope", securityConfig.getScope())
                    .addParameter("client_id", securityConfig.getClientId())
                    .addParameter("redirect_uri", securityConfig.getRedirectUri());
            redirectExtraParameters.forEach(parameterKey -> {
                uriBuilder.addParameter(parameterKey, securityConfig.getParameter(parameterKey));
            });
            return uriBuilder.build().toString();
        } catch (URISyntaxException exc) {
            logger.warn("Could not create redirect URI with secureity config: {}", securityConfig);
            throw new IdentityProviderException(exc.getMessage(), exc);
        }
    }

    @Override
    protected String decodeIDToken(String idToken, SecurityConfig securityConfig) throws Exception {
        return SignedJWT.parse(idToken).getJWTClaimsSet().toString().replace("\\", "");
    }

    /**
     * Uses a code from when a user has authorized Google to get some information about him to make a request
     * to the Google API.
     *
     * @param uri containing a code and maybe some more information about the request.
     * @return DefaultUserData object containing information about the user.
     * @throws IdentityProviderException
     */
    @Override
    public UserData getToken(String uri) throws IdentityProviderException {
        Map<String, String> urlParameters = URLEncodedUtils.parse(URI.create(uri), "UTF-8").stream()
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

        HttpPost postRequest = new HttpPost(APIURL);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("code", urlParameters.get("code")));
        params.add(new BasicNameValuePair("redirect_uri", securityConfig.getRedirectUri()));
        params.add(new BasicNameValuePair("client_id", securityConfig.getClientId()));
        params.add(new BasicNameValuePair("client_secret", securityConfig.getPassword()));
        params.add(new BasicNameValuePair("scope", securityConfig.getScope()));

        tokenExtraParameters.forEach(parameterKey -> {
            params.add(new BasicNameValuePair(parameterKey, securityConfig.getParameter(parameterKey)));
        });

        HttpResponse httpResponse;
        try {
            postRequest.setEntity(new UrlEncodedFormEntity(params));
            httpResponse = httpClient.execute(postRequest);
        } catch (UnsupportedEncodingException exc) {
            throw new IdentityProviderException(exc.getMessage(), exc);
        } catch (IOException exc) {
            logger.warn("Could not send post request to external server");
            throw new IdentityProviderException(exc.getMessage(), exc);
        }

        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IdentityProviderException("Bad response from IdentityProvider API");
        }

        JsonObject jsonResponse;

        try {
            String responseContent = IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");
            jsonResponse = gson.fromJson(responseContent, JsonObject.class);
            if (securityConfig.getJSONWebKeys() != null) {
                return new DefaultUserData(decodeIDToken(jsonResponse.get("id_token").getAsString(), securityConfig), jsonResponse.get("access_token").getAsString());
            }
            throw new IdentityProviderException("The IdentityProvider is not configured correctly");
        } catch (Exception exc) {
            logger.error("Could not read response from external server.");
            logger.error("Likely the server tried to us an old (unvalid) code to retrieve user's data from provider");
            throw new IdentityProviderException(exc.getMessage(), exc);
        }
    }
}

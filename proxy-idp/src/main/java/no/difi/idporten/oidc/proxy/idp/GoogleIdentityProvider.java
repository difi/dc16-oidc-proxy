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
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class GoogleIdentityProvider extends AbstractIdentityProvider {

    private static Logger logger = LoggerFactory.getLogger(GoogleIdentityProvider.class);

    private final SecurityConfig securityConfig;
    private final String APIURL = "https://www.googleapis.com/oauth2/v3/token";
    private final String LOGINURL = "https://accounts.google.com/o/oauth2/auth";

    public GoogleIdentityProvider(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
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
            return new URIBuilder(LOGINURL)
                    .addParameter("scope", securityConfig.getScope())
                    .addParameter("client_id", securityConfig.getClientId())
                    .addParameter("response_type", securityConfig.getParameter("response_type"))
                    .addParameter("access_type", securityConfig.getParameter("access_type"))
                    .addParameter("approval_prompt", securityConfig.getParameter("approval_prompt"))
                    .addParameter("redirect_uri", securityConfig.getRedirectUri())
                    .build().toString();
        } catch (URISyntaxException e) {
            throw new IdentityProviderException(e.getMessage(), e);
        }
    }

    /**
     * Uses a code from when a user has authorized Google to get some information about him to make a request
     * to the Google API.
     *
     * @param uri containing a code and maybe some more information about the request.
     * @return UserData object containing information about the user.
     * @throws IdentityProviderException
     */
    @Override
    public UserData getToken(String uri) throws IdentityProviderException {
        try {

            Map<String, String> urlParameters = URLEncodedUtils.parse(URI.create(uri), "UTF-8").stream()
                    .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));


            HttpPost postRequest = new HttpPost(APIURL);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("code", urlParameters.get("code")));
            params.add(new BasicNameValuePair("redirect_uri", securityConfig.getRedirectUri()));
            params.add(new BasicNameValuePair("client_id", securityConfig.getClientId()));
            params.add(new BasicNameValuePair("client_secret", securityConfig.getPassword()));
            params.add(new BasicNameValuePair("scope", securityConfig.getScope())); // orElse("")
            params.add(new BasicNameValuePair("grant_type", securityConfig.getParameter("grant_type")));
            postRequest.setEntity(new UrlEncodedFormEntity(params));

            logger.debug(String.format("Created post request:\n%s\n%s\n%s", postRequest, postRequest.getAllHeaders(), postRequest.getEntity()));

            HttpResponse httpResponse = httpClient.execute(postRequest);

            logger.debug("Sending 'POST' request to URL : " + APIURL);
            logger.debug("Post parameters : " + params);
            logger.debug("Response Code : " + httpResponse.getStatusLine().getStatusCode());
            logger.debug("Response message : " + httpResponse.getStatusLine().getReasonPhrase());

            JsonObject jsonResponse;
            try (InputStream inputStream = httpResponse.getEntity().getContent()) {
                jsonResponse = gson.fromJson(new InputStreamReader(inputStream), JsonObject.class);
                return new UserData(decodeIDToken(jsonResponse.get("id_token").getAsString()));
            } catch (IOException exc) {
                throw new IdentityProviderException(exc.getMessage(), exc);
            }
        } catch (Exception exc) {
            throw new IdentityProviderException(exc.getMessage(), exc);
        }
    }

    /**
     * Decodes a signed JWT token to a human-readable string.
     *
     * @param idToken
     * @return
     * @throws Exception
     */
    private String decodeIDToken(String idToken) throws Exception {
        return JWTParser.parse(idToken).getJWTClaimsSet().toString().replace("\\", "");
    }
}

package no.difi.idporten.oidc.proxy.idp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nimbusds.jwt.JWTParser;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

abstract class AbstractIdentityProvider implements IdentityProvider {

    protected static Gson gson = new GsonBuilder().create();

    protected static HttpClient httpClient = HttpClients.createDefault();

    /**
     * Decodes a signed JWT token to a human-readable string.
     *
     * @param idToken
     * @return
     * @throws Exception
     */
    protected String decodeIDToken(String idToken) throws Exception {
        return JWTParser.parse(idToken).getJWTClaimsSet().toString().replace("\\", "");
    }
}

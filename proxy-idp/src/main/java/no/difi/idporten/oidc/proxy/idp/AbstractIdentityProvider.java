package no.difi.idporten.oidc.proxy.idp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;

abstract class AbstractIdentityProvider implements IdentityProvider {

    protected static Gson gson = new GsonBuilder().create();

    /**
     * Decodes a signed JWT token to a human-readable string.
     *
     * @param idToken
     * @param jwkSet
     * @return
     * @throws Exception
     */
    protected String decodeIDToken(String idToken, JWKSet jwkSet) throws Exception {
        String kid = JWTParser.parse(idToken).getHeader().toJSONObject().get("kid").toString();

        List<JWK> matches = new JWKSelector(new JWKMatcher.Builder()
                .keyType(KeyType.RSA)
                .keyID(kid)
                .build())
                .select(jwkSet);
        SignedJWT signedJWT = SignedJWT.parse(idToken);

        for (JWK match : matches) {
            RSASSAVerifier rsassaVerifier = new RSASSAVerifier(((RSAKey) match).toRSAPublicKey());
            if (signedJWT.verify(rsassaVerifier)) {
                return signedJWT.getJWTClaimsSet().toString().replace("\\", "");
            }

        }
        throw new IdentityProviderException("The signature of the ID token was not correct");
    }

}

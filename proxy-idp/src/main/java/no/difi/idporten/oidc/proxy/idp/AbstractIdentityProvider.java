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
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

abstract class AbstractIdentityProvider implements IdentityProvider {

    protected static Gson gson = new GsonBuilder().create();

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

    protected void isVerfiedToken(String idToken, String endpointURI) throws Exception{
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(endpointURI);
        HttpResponse httpResponse = httpClient.execute(httpGet);

        String kid = JWTParser.parse(idToken).getHeader().toJSONObject().get("kid").toString();

        String content = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
        JSONObject jsonObject = JSONObjectUtils.parse(content);

        JWK jwk = JWK.parse(jsonObject.get("keys").toString().replace("[", "").replace("]", ""));
        JWKSet jwkSet = new JWKSet(jwk);

        List<JWK> matches = new JWKSelector(new JWKMatcher.Builder()
                .keyType(KeyType.RSA)
                .keyID(kid)
                .build())
                .select(jwkSet);

        SignedJWT signedJWT = SignedJWT.parse(idToken);

        RSASSAVerifier rsassaVerifier = new RSASSAVerifier(((RSAKey)matches.get(0)).toRSAPublicKey());
        System.out.println("TEST: " + signedJWT.verify(rsassaVerifier));
        signedJWT.verify(rsassaVerifier);


    }


}

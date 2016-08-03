package no.difi.idporten.oidc.proxy.idp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;

import java.util.Date;
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
        SignedJWT signedJWT = SignedJWT.parse(idToken);
        tokenHasCorrectIssuer(signedJWT);
        if (tokenHasValidSignature(idToken, jwkSet, signedJWT) && tokenTimesAreCorrect(signedJWT)) {
            return signedJWT.getJWTClaimsSet().toString().replace("\\", "");
        }
        throw new IdentityProviderException("The signature of the ID token was not correct");
    }

    private boolean tokenHasValidSignature(String idToken, JWKSet jwkSet, SignedJWT signedJWT) throws Exception {
        String kid = JWTParser.parse(idToken).getHeader().toJSONObject().get("kid").toString();

        List<JWK> matches = new JWKSelector(new JWKMatcher.Builder()
                .keyType(KeyType.RSA)
                .keyID(kid)
                .build())
                .select(jwkSet);

        for (JWK match : matches) {
            RSASSAVerifier rsassaVerifier = new RSASSAVerifier(((RSAKey) match).toRSAPublicKey());
            if (signedJWT.verify(rsassaVerifier)) {
                return true;
            }

        }
        return false;
    }

    private boolean tokenTimesAreCorrect(SignedJWT signedJWT) throws Exception {

        return signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date()) &&
                signedJWT.getJWTClaimsSet().getIssueTime().before(new Date());
    }

    private boolean tokenHasCorrectIssuer(SignedJWT signedJWT) throws Exception {
        System.out.println("HUHEI: " + signedJWT.getJWTClaimsSet().getIssuer());
        System.out.println(signedJWT.getJWTClaimsSet().getAudience());
        return true;
    }

}

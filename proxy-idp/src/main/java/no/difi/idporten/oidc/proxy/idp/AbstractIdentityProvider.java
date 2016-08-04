package no.difi.idporten.oidc.proxy.idp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;

import java.util.Date;
import java.util.List;

abstract class AbstractIdentityProvider implements IdentityProvider {

    protected static Gson gson = new GsonBuilder().create();

    /**
     * Decodes a signed JWT token to a human-readable string if the token is valid.
     *
     * @param idToken:
     * @return
     * @throws Exception
     */
    protected String decodeIDToken(String idToken, SecurityConfig securityConfig) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(idToken);
        if (isValidToken(idToken, signedJWT, securityConfig)) {
            return signedJWT.getJWTClaimsSet().toString().replace("\\", "");
        }
        throw new IdentityProviderException("The token could not be validated.");
    }

    /**
     * Checks if the token is valid
     *
     * @param idToken:
     * @param signedJWT:
     * @param securityConfig:
     * @return
     * @throws Exception
     */
    private boolean isValidToken(String idToken, SignedJWT signedJWT, SecurityConfig securityConfig) throws Exception {
        JWKSet jwkSet = securityConfig.getJSONWebKeys();
        String kid = JWTParser.parse(idToken).getHeader().toJSONObject().get("kid").toString();
        String issuer = securityConfig.getIssuer();
        String clientId = securityConfig.getClientId();
        return tokenHasCorrectAudience(signedJWT, clientId) &&
                tokenHasCorrectIssuer(signedJWT, issuer) &&
                tokenHasValidSignature(kid, jwkSet, signedJWT) &&
                tokenTimesAreCorrect(signedJWT);
    }

    /**
     * Checks the signature of the token
     *
     * @param kid:
     * @param jwkSet:
     * @param signedJWT:
     * @return
     * @throws Exception
     */
    private boolean tokenHasValidSignature(String kid, JWKSet jwkSet, SignedJWT signedJWT) throws Exception {
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

    /**
     * Checks the expires and iat parameteres of the token. Expires must be in the future and iat must be in the past.
     *
     * @param signedJWT:
     * @return
     * @throws Exception
     */
    private boolean tokenTimesAreCorrect(SignedJWT signedJWT) throws Exception {
        return signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date()) &&
                signedJWT.getJWTClaimsSet().getIssueTime().before(new Date());
    }

    /**
     * Checks the issuer of the token.
     *
     * @param signedJWT:
     * @param issuer:
     * @return
     * @throws Exception
     */
    private boolean tokenHasCorrectIssuer(SignedJWT signedJWT, String issuer) throws Exception {
        return signedJWT.getJWTClaimsSet().getIssuer().equals(issuer);
    }

    /**
     * Checks the audience of the token.
     *
     * @param signedJWT:
     * @param clientId:
     * @return
     * @throws Exception
     */
    private boolean tokenHasCorrectAudience(SignedJWT signedJWT, String clientId) throws Exception {
        return signedJWT.getJWTClaimsSet().getAudience().contains(clientId);
    }

}

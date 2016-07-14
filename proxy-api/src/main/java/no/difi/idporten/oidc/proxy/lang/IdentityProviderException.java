package no.difi.idporten.oidc.proxy.lang;

public class IdentityProviderException extends Exception {

    public IdentityProviderException(String message) {
        super(message);
    }

    public IdentityProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}

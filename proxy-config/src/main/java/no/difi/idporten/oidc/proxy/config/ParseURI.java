package no.difi.idporten.oidc.proxy.config;

import no.difi.idporten.oidc.proxy.model.Host;

import java.net.URI;

/**
 * Class for parsing URIs. Can be used to extract information from any URI.
 */

public class ParseURI {

    private URI uri;

    private String scheme;
    private String shcemeSpecificPart;
    private String authority;
    private Host host;
    private int port;
    private String path;
    private String query;
    private String fragment;

    /**
     * Constructor. Sets the uri-field with a URI from the class "URICollection".
     * URI has to be added to the class "URICollection" before it can be parsed.
     */
    public ParseURI(int i){

        URICollection uriCollection = new URICollection();

        this.uri = uriCollection.getURI(i);
    }

    private void uriParser(){
        this.scheme = uri.getScheme();
        this.shcemeSpecificPart = uri.getSchemeSpecificPart();
        this.authority = uri.getAuthority();
        this.host = new Host(uri.getHost());
        this.port = uri.getPort();
        this.path = uri.getPath();
        this.query = uri.getQuery();
        this.fragment = uri.getFragment();

    }

    /**
     * Getters for URI-components
     */

    public URI getUri() {
        return this.uri;
    }

    public String getScheme() {
        return this.scheme;
    }

    public String getShcemeSpecificPart() {
        return this.shcemeSpecificPart;
    }

    public String getAuthority() {
        return this.authority;
    }

    public Host getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getPath() {
        return this.path;
    }

    public String getQuery() {
        return this.query;
    }

    public String getFragment() {
        return this.fragment;
    }
}

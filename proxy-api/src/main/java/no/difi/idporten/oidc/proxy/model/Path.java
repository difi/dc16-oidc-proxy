package no.difi.idporten.oidc.proxy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Path {

    private String path;
    private String redirectUri;
    private String scope;
    private Map<String, String> additional;

    public Path() {
        this("/", "", "");
    }

    public Path(String path) {
        this(path, "", "");

    }

    public Path(String path, String redirectUri, String scope) {
        this.path = path;
        this.redirectUri = redirectUri;
        this.scope = scope;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "Path{" +
                "path='" + path + '\'' +
                ", redirectUri='" + redirectUri + '\'' +
                ", scope='" + scope + '\'' +
                ", additional=" + additional +
                '}';
    }
}

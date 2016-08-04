package no.difi.idporten.oidc.proxy.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.typesafe.config.Config;
import no.difi.idporten.oidc.proxy.model.IdpConfig;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TypesafeIdpConfig implements IdpConfig {

    private static Logger logger = LoggerFactory.getLogger(TypesafeIdpConfig.class);

    private String identifier;

    private String idpClass;

    private String clientId;

    private String password;

    private String scope;

    private String redirectUri;

    private Map<String, String> parameters;

    private Optional<String> passAlongData;

    private List<String> userDataNames;

    private Optional<JWKSet> JSONWebKeys;

    private Optional<String> jwkUri;

    private Optional<String> issuer;

    private Optional<String> apiUri;

    private Optional<String> loginUri;


    public TypesafeIdpConfig(String identifier, Config idpConfig) {
        this.identifier = identifier;
        this.idpClass = idpConfig.getString("class");
        this.clientId = idpConfig.getString("client_id");
        this.password = idpConfig.getString("password");
        this.scope = idpConfig.getString("scope");
        this.redirectUri = idpConfig.getString("redirect_uri");
        this.parameters = idpConfig.getConfig("parameters").entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().unwrapped().toString()));

        if (checkForStringInConfig(idpConfig.getString("pass_along_data"),idpConfig)){
            this.passAlongData = Optional.of(idpConfig.getString("pass_along_data"));
        }
        if (checkForStringInConfig(idpConfig.getString("user_data_name"),idpConfig)){
            this.userDataNames = idpConfig.getStringList("user_data_names");
        }
        if (checkForStringInConfig(idpConfig.getString("jwk_uri"),idpConfig)){
            this.jwkUri = Optional.of(idpConfig.getString("jwk_uri"));
            this.JSONWebKeys = Optional.of(getJWKsFromConfig(idpConfig.getString("jwk_uri")));
        }
        if (checkForStringInConfig(idpConfig.getString("issuer"),idpConfig)){
            this.issuer = Optional.of(idpConfig.getString("issuer"));
        }
        if (checkForStringInConfig(idpConfig.getString("api_uri"),idpConfig)){
            this.apiUri = Optional.of(idpConfig.getString("api_uri"));
        }
        if (checkForStringInConfig(idpConfig.getString("login_uri"),idpConfig)){
            this.loginUri = Optional.of(idpConfig.getString("login_uri"));
        }
        logger.debug("Created IdpConfig:\n{}", this);
    }

    private boolean checkForStringInConfig(String stringToBeChecked, Config config) {
        return (config.entrySet().toString().contains(stringToBeChecked));
    }

    private JWKSet getJWKsFromConfig(String jwkUri) {
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(jwkUri);
            HttpResponse httpResponse = httpClient.execute(httpGet);

            String content = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            return JWKSet.parse(content);
        } catch (Exception e) {
            logger.info("Received '{}'.", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Optional<String> getJwkUri() {
        return jwkUri;
    }

    @Override
    public Optional<String> getLoginUri() {
        return loginUri;
    }

    @Override
    public Optional<String> getIssuer() {
        return issuer;
    }

    @Override
    public Optional<String> getApiUri() {
        return apiUri;
    }

    @Override
    public Optional<JWKSet> getJSONWebKeys() {
        return JSONWebKeys;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getIdpClass() {
        return this.idpClass;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getScope() {
        return this.scope;
    }

    @Override
    public String getRedirectUri() {
        return this.redirectUri;
    }

    @Override
    public String getClientId() {
        return this.clientId;
    }

    @Override
    public Optional<String> getPassAlongData() {
        return this.passAlongData;
    }

    @Override
    public List<String> getUserDataNames() {
        return this.userDataNames;
    }

    @Override
    public Optional<String> getParameter(String key) {
        return parameters.containsKey(key) ? Optional.of(parameters.get(key)) : Optional.empty();
    }

    @Override
    public String toString() {
        return "TypesafeIdpConfig{" +
                "identifier='" + identifier + "'" +
                ", idpClass='" + idpClass + "'" +
                ", client_id='" + clientId + "'" +
                ", password='" + password + "'" +
                ", scope='" + scope + "'" +
                ", redirect_uri='" + redirectUri + "'" +
                ", parameters='" + parameters + "'" +
                ", pass_along_data='" + passAlongData + "'" +
                ", user_data_names" + userDataNames +
                "}";
    }
}

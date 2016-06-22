package httptest.idp;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

public class IdportenUrl extends GenericUrl {

    @Key
    private String scope;
    @Key
    private String client_id;
    @Key
    private String response_type;
    @Key
    private String redirect_uri;


    public IdportenUrl(String encodedUrl){
        super(encodedUrl);
    }

    public String getScope(){
        return scope;
    }

    public void setScope(String scope){
        this.scope = scope;
    }

    public void setClient_id(String client_id){
        this.client_id = client_id;
    }

    public String getClient_id(){
        return client_id;
    }
    public String getRedirect_uri() {
        return redirect_uri;
    }

    public void setRedirect_uri(String redirect_uri) {
        this.redirect_uri = redirect_uri;
    }
    public String getResponse_type() {
        return response_type;
    }

    public void setResponse_type(String response_type) {
        this.response_type = response_type;
    }


    public static void main(String[] args) {
        IdportenUrl u = new IdportenUrl("https://eid-exttest.difi.no/opensso/oauth2/authorize");
        u.setScope("openid");
        u.setClient_id("dificamp");
        u.setResponse_type("code");
        u.setRedirect_uri("http://localhost:8080/");
        System.out.println(u);
    }


}

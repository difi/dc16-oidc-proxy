package httptest.idp;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

import java.util.Map;

public class IdportenUrl extends GenericUrl {

    //Assisting class for IdportenIdentityProvider.
    //Creates a url given url-string, and a set of parameters.


    //The @Keys functions as parameters for the url. It is possible to add several other keys.

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

    public String getClient_id(){
        return client_id;
    }

    public void setClient_id(String client_id){
        this.client_id = client_id;
    }

    public String getResponse_type() {
        return response_type;
    }

    public void setRedirect_uri(String redirect_uri) {
        this.redirect_uri = redirect_uri;
    }

    public String getRedirect_uri() {
        return redirect_uri;
    }

    public void setResponse_type(String response_type) {
        this.response_type = response_type;
    }




}

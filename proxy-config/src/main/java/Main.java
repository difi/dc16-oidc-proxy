import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Created by camp-eul on 16.06.2016.
 */
public class Main {
    String hostname = "default";
    String level = "#3";

    public static void main(String args[]) throws Exception {

    }

    public static Config parseUrl(String url) {
        Config conf  = ConfigFactory.load();
        return conf;
    }
    
    }

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Main {
    String hostname = "default";
    String level = "#3";

    public static void main(String args[]) throws Exception {

    }

    public static Config makeConfig(String hostname) {


        Config config = ConfigFactory.empty();
        // config.entrySet("Hostname",hostname);

        return config;
    }

}

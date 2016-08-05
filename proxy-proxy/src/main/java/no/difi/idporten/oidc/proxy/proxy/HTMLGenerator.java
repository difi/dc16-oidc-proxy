package no.difi.idporten.oidc.proxy.proxy;

import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HTMLGenerator {

    private static Logger logger = LoggerFactory.getLogger(HTMLGenerator.class);

    private final static String defaultErrorPageUrl = "error-page.html";


    /**
     * Returns an error page as a string with the message inserted into the HTML template.
     * Gets URL for the HTML template from the config using SecurityConfig.
     *
     * @param message
     * @param securityConfig
     * @return
     */
    public static String getErrorPage(String message, SecurityConfig securityConfig) {
        String errorPageUrl = securityConfig.getErrorPageUrl().orElse(defaultErrorPageUrl);
        try {
            return getErrorPage(message, errorPageUrl);
        } catch (NullPointerException exc) {
            logger.warn("Could not find file for error page HTML template {}", errorPageUrl);
            return getErrorPage(message);
        } catch (FileNotFoundException exc) {
            return String.format("No cannot %s", message);
        }
    }

    /**
     * Returns an error page as a string with the message inserted into the default HTML template.
     *
     * @param message
     * @return
     */
    public static String getErrorPage(String message) {
        try {
            return getErrorPage(message, defaultErrorPageUrl);
        } catch (Exception exc) {
            return String.format("No cannot %s", message);
        }
    }

    /**
     * Utility function to get the HTML error page.
     *
     * @param message
     * @param errorPageUrl
     * @return
     * @throws NullPointerException
     * @throws FileNotFoundException
     */
    private static String getErrorPage(String message, String errorPageUrl) throws NullPointerException, FileNotFoundException {
        File file = new File(HTMLGenerator.class.getClassLoader().getResource(errorPageUrl).getFile());
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        return bufferedReader.lines().map(line -> replaceVariable(line, message)).collect(Collectors.joining("\n"));
    }

    /**
     * Replaces patterns matching {{variableName}} in a line and returns with the curly braces and variable name replaced.
     *
     * @param line:
     * @param message:
     * @return
     */
    private static String replaceVariable(String line, String message) {
        Pattern regex = Pattern.compile("\\{\\{(\\w+)\\}\\}");
        Matcher matcher = regex.matcher(line);
        if (matcher.find()) {
            return matcher.replaceAll(String.format("%s", message));
        }
        return line;
    }
}

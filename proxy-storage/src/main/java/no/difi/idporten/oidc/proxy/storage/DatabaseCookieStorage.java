package no.difi.idporten.oidc.proxy.storage;

import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DatabaseCookieStorage implements CookieStorage {

    private static Logger logger = LoggerFactory.getLogger(DatabaseCookieStorage.class);

    private CookieDatabase db;

    private static DatabaseCookieStorage ourInstance = new DatabaseCookieStorage();

    public static DatabaseCookieStorage getInstance() {
        return ourInstance;
    }

    private DatabaseCookieStorage() {
        logger.debug("DatabaseCookieStorage instantiated");
        db = new CookieDatabase();
    }

    /**
     * Invoked in InboundHandlerAdapter when the proxy server receives a JWT from the authorization server.
     * Creates a ProxyCookie object, inputs it in the database (through the CookieDatabase class),
     * and returns the object. The cookie is available for the user only if the time of the request
     * precedes created [Date] + maxExpiry [int] and lastUpdated [Date] + touchPeriod [int].
     *
     * @param uuid        String
     * @param cookieName  String (e.g. "google-cookie")
     * @param host        String (e.g. "www.google.com")
     * @param idp         String (e.g. "google")
     * @param touchPeriod int (in minutes)
     * @param userData    HashMap<String, String> (JWT from authorization server)
     * @return ProxyCookie (DefaultProxyCookie) object
     */
    public ProxyCookie generateCookieInDb(String uuid, String cookieName, String host, String idp, int security, int touchPeriod, int maxExpiry, Map<String, String> userData) {
        ProxyCookie proxyCookie = new DefaultProxyCookie(uuid, cookieName, host, idp, security, touchPeriod, maxExpiry, userData);
        return generateCookieInDb(proxyCookie);
    }

    public ProxyCookie generateCookieInDb(String cookieName, String host, String idp, int security, int touchPeriod, int maxExpiry, Map<String, String> userData) {
        return generateCookieInDb(UUID.randomUUID().toString(), cookieName, host, idp, security, touchPeriod, maxExpiry, userData);
    }

    /**
     * Used for debug and testing. Enables using other values self-assigned values to created and
     * lastUpdated, instead of time of instantiation.
     *
     * @param proxyCookie object
     * @return same proxyCookie object unchanged
     */
    public ProxyCookie generateCookieInDb(ProxyCookie proxyCookie) {
        db.insertCookie(proxyCookie);
        logger.info("Cookie generated and inserted in database ({})", proxyCookie.toString());
        return proxyCookie;
    }

    /**
     * 
     *
     * @param uuid
     * @param host
     * @param preferredIdpData
     * @return
     */
    @Override
    public Optional<ProxyCookie> findCookie(String uuid, String host, List<Map.Entry<String, String>> preferredIdpData) {
        logger.debug("Searching for cookie with uuid ({}) and host ({})", uuid, host);
        Optional<List<ProxyCookie>> result = db.findCookies(uuid);

        // Check if cookies are found
        if (result.isPresent() && !result.get().isEmpty()) {
            // Only include ones that are valid and has the host we're looking for
            List<ProxyCookie> cookies = result.get().stream().filter(ProxyCookie::isValid).filter(pc -> pc.getHost().equals(host)).collect(Collectors.toList());

            // There are still valid cookies left
            if (!cookies.isEmpty()) {
                // Idp of valid cookies, in same order
                List<String> cookieIdps = cookies.stream().map(ProxyCookie::getIdp).collect(Collectors.toList());
                logger.debug("Given uuid ({}) has valid cookie(s) with idp: {}", uuid, cookieIdps.toString());

                // Our preferred IDPs, but as a list with only the keys (IPD names). To be able to compare if any matching elements with cookieIdps list
                List<String> desiredIdps = preferredIdpData.stream().map(Map.Entry::getKey).collect(Collectors.toList());
                logger.debug("Crosschecking with our preferred idps: {}");

                //We have a cookie for at least one of our preferred IDPs
                if (!Collections.disjoint(cookieIdps, desiredIdps)) {
                    // Another way of achieving the same effect: !CollectionUtils.containsAny(cookieIdps, desiredIdps)

                    ProxyCookie proxyCookie = null;
                    Map<String, String> userData = new HashMap<>();
                    boolean primaryUserDataFound = false;

                    // Add cookie's userData if we have have any of the preferred cookies
                    for (Map.Entry<String, String> preferredIdp : preferredIdpData) {
                        logger.debug("Looking for cookie with idp: {}", preferredIdp.getKey());
                        if (cookieIdps.contains(preferredIdp.getKey()) && !primaryUserDataFound) {
                            logger.debug("Making cookie with this idp ({}) our primary cookie. Adding userData if exists", preferredIdp.getKey());
                            proxyCookie = cookies.get(cookieIdps.indexOf(preferredIdp.getKey()));
                            if (proxyCookie.getUserData() != null) userData.putAll(proxyCookie.getUserData());
                            primaryUserDataFound = true;
                            extendCookieExpiry(proxyCookie);
                        } else if (cookieIdps.contains(preferredIdp.getKey()) && !userData.containsKey(preferredIdp.getKey())) {
                            logger.debug("Found cookie with idp ({}), but another idp was more preferable ", preferredIdp.getKey());
                            extendCookieExpiry(cookies.get(cookieIdps.indexOf(preferredIdp.getKey())));
                            String additionalData = cookies.get(cookieIdps.indexOf(preferredIdp.getKey())).getUserData().get(preferredIdp.getValue());
                            if (additionalData != null) {
                                userData.put("X-additional-data/" + preferredIdp.getKey() + "-" + preferredIdp.getValue(), additionalData);
                                logger.debug("Adding pass-along-data of idp {}: {}={}", preferredIdp.getKey(), preferredIdp.getValue(), additionalData);
                            }
                        } else {
                            logger.debug("Preferred idp {} has no cookie in the database", preferredIdp.getKey());
                        }
                    }

                    if (proxyCookie != null) {
                        ((DefaultProxyCookie) proxyCookie).setUserData(userData);
                        logger.info("Returning valid cookie from database: {}", proxyCookie.toString());
                        logger.debug("Cookie has userData: ", proxyCookie.getUserData());
                        return Optional.of(proxyCookie);
                    }
                } else {
                    logger.info("Cookie(s) with given uuid ({}) was found, but preferred idp(s) and found ipd(s) doesn't match", uuid);
                }
            } else {
                logger.info("Cookie(s) with given uuid ({}) was found, but are not valid and/or has correct host", uuid);
            }
        } else {
            logger.info("Cookie(s) with given uuid ({)) was not found", uuid);
        }
        return Optional.empty();
    }

    /**
     * When the user logs in again, the session's cookie expiry is expanded. Expands cookie expiry by
     * updating the lastUpdated variable in the cookie, to present time. Does not handle the instance
     * where new expiry exceeds max expiry here, both of these are validated before returning a found
     * cookie is returned.
     *
     * @param proxyCookie object
     */
    private ProxyCookie extendCookieExpiry(ProxyCookie proxyCookie) {
        Date now = new Date();
        Date touchExpiry = calculateDate(proxyCookie.getLastUpdated(), proxyCookie.getTouchPeriod());
        Date maxExpiry = calculateDate(proxyCookie.getCreated(), proxyCookie.getMaxExpiry());

        if (now.after(touchExpiry) || now.after(maxExpiry)) {
            logger.warn("Cannot extend expiry of expired cookie ({})", proxyCookie);
            return proxyCookie;
        }
        ((DefaultProxyCookie) proxyCookie).setLastUpdated(now);
        db.extendCookieExpiry(proxyCookie.getUuid(), proxyCookie.getIdp(), now);
        logger.debug("Extended expiry of cookie ({})", proxyCookie);
        return proxyCookie;
    }

    @Override
    public void removeExpiredCookies() {
        db.removeExpiredCookies();
        logger.info("Removed expired cookies");
    }

    @Override
    public void removeCookie(String uuid) {
        db.removeCookie(uuid);
        logger.info("Removed cookie if it existed (UUID: " + uuid + ")");
    }

    // Debug
    public void printAllCookies() {
        db.getAllCookies().values().forEach(CookieDatabase::printCookie);
    }

    private static Date calculateDate(Date date, int minutes) {
        return new Date(date.getTime() + minutes * 60 * 1000);
    }

    /* Debug and testing
    public static void main(String[] args) {
        List<Map.Entry<String, String>> prefIdps = new ArrayList<>(Arrays.asList(
                new AbstractMap.SimpleEntry<>("google", "email"),
                new AbstractMap.SimpleEntry<>("idporten", "pid")));

        List<Map.Entry<String, String>> prefIdps2 = new ArrayList<>(Arrays.asList(
                new AbstractMap.SimpleEntry<>("idporten", "pid"),
                new AbstractMap.SimpleEntry<>("google", "email")));


        Map<String, String> userData = new HashMap<>();
        userData.put("email", "mail@gmail.com");
        userData.put("at_hash", "gB6153asgvrA7Casdas22w");
        userData.put("sub", "99999999999999999999");

        Map<String, String> userData2 = new HashMap<>();
        userData2.put("pid", "12345600000");
        userData2.put("exp", "11111111111");
        userData2.put("sub", "2222222222222222222");


        ProxyCookie pc = new DefaultProxyCookie("uuid", "localhost-cookie", "localhost", "google", 4, 30, 120, userData);
        ProxyCookie pc2 = new DefaultProxyCookie("uuid", "localhost-cookie", "localhost", "idporten", 2, 10, 60, userData2, new Date(), new Date());

        ProxyCookie pc3 = new DefaultProxyCookie("uuid2", "localhost-cookie", "localhost", "google", 4, 30, 120, userData);

        //ourInstance.generateCookieInDb("someCookie", "host.com", "/", 3, 20, 100, new HashMap<>(1));

        ourInstance.generateCookieInDb(pc);
        ourInstance.generateCookieInDb(pc2);
        ourInstance.generateCookieInDb(pc3);

        System.err.println("\nCookies inserted to db. Printing\n");
        ourInstance.printAllCookies();
        Optional<ProxyCookie> pcc = ourInstance.findCookie("uuid", "localhost", prefIdps);
        Optional<ProxyCookie> pcc2 = ourInstance.findCookie("uuid", "localhost", prefIdps2);
//        System.err.println("\nFirst 2 cookies. Printing found objects");

//        if (pcc.isPresent()) System.out.println(pcc.get());
//        else System.err.println("Didn't find cookie");
//        if (pcc2.isPresent()) System.out.println(pcc2.get());
//        else System.err.println("Didn't find cookie");

        ourInstance.findCookie("uuid2", "localhost", prefIdps2);
        System.out.println("Finding non-existent cookie: ");
        ourInstance.findCookie("sup", "localhost", prefIdps2);

    }
*/


}

package no.difi.idporten.oidc.proxy.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import no.difi.idporten.oidc.proxy.api.CookieStorage;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class DatabaseCookieStorageTest {

    private static final int MINUTE = 1000 * 60;

    private int touchPeriod = 20;

    private int maxExpiry = 120;

    private int security3 = 3;

    private int security4 = 4;

    private String idpGoogle = "google";

    private String idpIdporten = "idporten";

    private String idpTwitter = "twitter";

//    CookieStorage cookieStorage;

    private HashMap<String, String> userDataIdporten = new HashMap<>();

    private HashMap<String, String> userDataGoogle = new HashMap<>();

    private HashMap<String, String> userDataTwitter = new HashMap<>();

    List<Map.Entry<String, String>> prefIdpsGoogleIdporten = new ArrayList<>(Arrays.asList(
            new AbstractMap.SimpleEntry<>("google", "email"),
            new AbstractMap.SimpleEntry<>("idporten", "pid")));

    List<Map.Entry<String, String>> prefIdpsIdportenGoogle = new ArrayList<>(Arrays.asList(
            new AbstractMap.SimpleEntry<>("idporten", "pid"),
            new AbstractMap.SimpleEntry<>("google", "email")));

    List<Map.Entry<String, String>> prefIdpsGoogleTwitterIdporten= new ArrayList<>(Arrays.asList(
            new AbstractMap.SimpleEntry<>("google", "email"),
            new AbstractMap.SimpleEntry<>("twitter", "username"),
            new AbstractMap.SimpleEntry<>("idporten", "pid")));

    List<Map.Entry<String, String>> prefIdpsAmazonInstagram = new ArrayList<>(Arrays.asList(
            new AbstractMap.SimpleEntry<>("amazon", "mail"),
            new AbstractMap.SimpleEntry<>("instagram", "username")));

    List<Map.Entry<String, String>> prefIdpsIdportenValueEmptyGoogle = new ArrayList<>(Arrays.asList(
            new AbstractMap.SimpleEntry<>("idporten", ""),
            new AbstractMap.SimpleEntry<>("google", "email")));

    List<Map.Entry<String, String>> prefIdpsIdportenGoogleValueEmpty = new ArrayList<>(Arrays.asList(
            new AbstractMap.SimpleEntry<>("idporten", "pid"),
            new AbstractMap.SimpleEntry<>("google", "")));

    private Injector injector;

    @Test
    public void beforeClass() {
//        cookieStorage = injector.getInstance(CookieStorage.class);
        injector = Guice.createInjector(new StorageModule());

        userDataIdporten.put("pid", "12345600000");
        userDataIdporten.put("aud", "abcdefghij");
        userDataIdporten.put("sub", "1234567890");

        userDataGoogle.put("email", "mail@gmail.com");
        userDataGoogle.put("at_hash", "gB6153asgvrA7Casdas22w");
        userDataGoogle.put("sub", "999999999999999999");

        userDataTwitter.put("username", "helloworld");
        userDataTwitter.put("city", "leikanger");
        userDataTwitter.put("company", "difi");
    }


    @Test
    public void createSimpleCookieInStorage() {
        CookieStorage cookieStorage = cookieStorage = injector.getInstance(CookieStorage.class);

        ProxyCookie proxyCookie = cookieStorage.generateCookieInDb("PROXYCOOKIE", "example.com", idpGoogle, security3, touchPeriod, maxExpiry, null);
        Assert.assertNotNull(proxyCookie);

        Assert.assertTrue(cookieStorage.findCookie(proxyCookie.getUuid(), "example.com", prefIdpsIdportenGoogle).isPresent());
        Assert.assertTrue(cookieStorage.findCookie(proxyCookie.getUuid(), "example.com", prefIdpsGoogleIdporten).isPresent());
        Assert.assertTrue(cookieStorage.findCookie(proxyCookie.getUuid(), "example.com", prefIdpsGoogleTwitterIdporten).isPresent());
        // Check with both Google and Idporten as preferred idp, when their pass_along_data doesn't exist
        Assert.assertTrue(cookieStorage.findCookie(proxyCookie.getUuid(), "example.com", prefIdpsIdportenValueEmptyGoogle).isPresent());
        Assert.assertFalse(cookieStorage.findCookie(proxyCookie.getUuid(), "domain.com", prefIdpsIdportenGoogleValueEmpty).isPresent());
        // When none of the preferred idps match the one in the database
        Assert.assertFalse(cookieStorage.findCookie(proxyCookie.getUuid(), "example.com", prefIdpsAmazonInstagram).isPresent());
    }

    @Test
    public void getPreferredUserDataAndAdditionalData() {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);
        ProxyCookie proxyCookieIdportenIdp = cookieStorage.generateCookieInDb("uuid", "PROXYCOOKIE", "localhost", idpGoogle, security3, touchPeriod, maxExpiry, userDataGoogle);
        ProxyCookie proxyCookieGoogleIdp = cookieStorage.generateCookieInDb("uuid", "PROXYCOOKIE", "localhost", idpIdporten, security4, touchPeriod, maxExpiry, userDataIdporten);
        ProxyCookie proxyCookieTwitterIdp = cookieStorage.generateCookieInDb("uuid", "PROXYCOOKIE", "localhost", idpTwitter, security4, touchPeriod, maxExpiry, userDataTwitter);

        Optional<ProxyCookie> findCookiePrefIdporten = cookieStorage.findCookie(proxyCookieIdportenIdp.getUuid(), proxyCookieIdportenIdp.getHost(), prefIdpsIdportenGoogle);
        Optional<ProxyCookie> findCookiePrefGoogle = cookieStorage.findCookie(proxyCookieGoogleIdp.getUuid(), proxyCookieGoogleIdp.getHost(), prefIdpsGoogleTwitterIdporten);
        Optional<ProxyCookie> findCookiePrefAmazon = cookieStorage.findCookie(proxyCookieGoogleIdp.getUuid(), proxyCookieGoogleIdp.getHost(), prefIdpsAmazonInstagram);

        String idportenAdditionalDataKey = "X-additional-data/" + prefIdpsIdportenGoogle.get(0).getKey() + "-" + prefIdpsIdportenGoogle.get(0).getValue();
        String googleAdditionalDataKey = "X-additional-data/" + prefIdpsGoogleTwitterIdporten.get(0).getKey() + "-" + prefIdpsGoogleTwitterIdporten.get(0).getValue();
        String twitterAdditionalDataKey = "X-additional-data/" + prefIdpsGoogleTwitterIdporten.get(1).getKey() + "-" + prefIdpsGoogleTwitterIdporten.get(1).getValue();

        System.out.println("Additional data keys:\n"+idportenAdditionalDataKey+"\n"+googleAdditionalDataKey+"\n"+twitterAdditionalDataKey);

        Assert.assertTrue(findCookiePrefIdporten.isPresent()); // Not including Twitter as idp
        Assert.assertTrue(findCookiePrefGoogle.isPresent());  // Including Twitter as idp
        Assert.assertFalse(findCookiePrefAmazon.isPresent());  // Not including any idps know to cookie


        System.out.println("\n\nFOUND COOKIE IN TEST WITH IDPORTEN PREFERRED:");
        CookieDatabase.printCookie(findCookiePrefIdporten.get());

        // Checking found cookie for userData with Idporten as preferred idp and Google second, not including Twitter
        // UserData with preferred idp Idporten contains all Idporten data and not Google
        Assert.assertTrue(findCookiePrefIdporten.get().getUserData().keySet().containsAll(userDataIdporten.keySet()));
        Assert.assertTrue(findCookiePrefIdporten.get().getUserData().values().containsAll(userDataIdporten.values()));
        Assert.assertFalse(findCookiePrefIdporten.get().getUserData().keySet().containsAll(userDataGoogle.keySet()));
        Assert.assertFalse(findCookiePrefIdporten.get().getUserData().values().containsAll(userDataGoogle.values()));

        // UserData with preferred idp Idporten contains additional Google data
        Assert.assertFalse(findCookiePrefIdporten.get().getUserData().containsKey(idportenAdditionalDataKey));
        Assert.assertTrue(findCookiePrefIdporten.get().getUserData().containsKey(googleAdditionalDataKey));

        // Contains correct additional value for Google
        Assert.assertEquals(findCookiePrefIdporten.get().getUserData().get(googleAdditionalDataKey), userDataGoogle.get(prefIdpsGoogleTwitterIdporten.get(0).getValue()));

        // We looked for cookies with preferred idp Idporten first, including Google and not including Twitter. Check Twitter data is not included in userData
        Assert.assertFalse(findCookiePrefIdporten.get().getUserData().containsKey(twitterAdditionalDataKey));

        System.out.println("\n\nFOUND COOKIE IN TEST WITH GOOGLE PREFERRED:");
        CookieDatabase.printCookie(findCookiePrefGoogle.get());

        // Checking found cookie for userData with Google as preferred idp, Twitter second, and Idporten third
        // UserData with preferred idp Google contains all Google data, and not Idporten and Twitter
        Assert.assertTrue(findCookiePrefGoogle.get().getUserData().keySet().containsAll(userDataGoogle.keySet()));
        Assert.assertTrue(findCookiePrefGoogle.get().getUserData().values().containsAll(userDataGoogle.values()));
        Assert.assertFalse(findCookiePrefGoogle.get().getUserData().keySet().containsAll(userDataIdporten.keySet()));
        Assert.assertFalse(findCookiePrefGoogle.get().getUserData().values().containsAll(userDataIdporten.values()));
        Assert.assertFalse(findCookiePrefGoogle.get().getUserData().keySet().containsAll(userDataTwitter.keySet()));
        Assert.assertFalse(findCookiePrefGoogle.get().getUserData().values().containsAll(userDataTwitter.values()));

        // UserData with preferred idp Google contains additional Google and Twitter data
        Assert.assertFalse(findCookiePrefGoogle.get().getUserData().containsKey(googleAdditionalDataKey));
        Assert.assertTrue(findCookiePrefGoogle.get().getUserData().containsKey(idportenAdditionalDataKey));
        Assert.assertTrue(findCookiePrefGoogle.get().getUserData().containsKey(twitterAdditionalDataKey));

        // Contains correct additional values for Idporten and Twitter
        Assert.assertEquals(findCookiePrefGoogle.get().getUserData().get(twitterAdditionalDataKey), userDataTwitter.get(prefIdpsGoogleTwitterIdporten.get(1).getValue()));
        Assert.assertEquals(findCookiePrefGoogle.get().getUserData().get(idportenAdditionalDataKey), userDataIdporten.get(prefIdpsGoogleTwitterIdporten.get(2).getValue()));
    }


    @Test
    public void findingCookieExtendsExpiryDate() throws Exception {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);
        
        Date now = new Date();

        ProxyCookie proxyCookie = new DefaultProxyCookie(UUID.randomUUID().toString(), "proxyCookie", "example.com", idpGoogle, security3, touchPeriod, maxExpiry, null);
        ProxyCookie expiresSoon = new DefaultProxyCookie(UUID.randomUUID().toString(), "expiresSoon", "example.com", idpIdporten, security4, touchPeriod, maxExpiry, null, 
                                                                                                            calculateDate(now, -(maxExpiry - 5)), calculateDate(now, -5));
        // Input cookies to database
        ((DatabaseCookieStorage) cookieStorage).generateCookieInDb(expiresSoon);
        ((DatabaseCookieStorage) cookieStorage).generateCookieInDb(proxyCookie);

        // Declaring maxExpiry and expiry for proxyCookie and expiresSoon at time of instantiation
        Date initialProxyCookieExpiry = calculateDate(proxyCookie.getLastUpdated(), proxyCookie.getTouchPeriod());
        Date initialProxyCookieMaxExpiry = calculateDate(proxyCookie.getCreated(), proxyCookie.getMaxExpiry());
        Date initialExpiresSoonMaxExpiry = calculateDate(expiresSoon.getCreated(), expiresSoon.getMaxExpiry());
        Date initialExpiresSoonExpiry = calculateDate(expiresSoon.getLastUpdated(), expiresSoon.getTouchPeriod());

        Thread.sleep(1000);

        Optional<ProxyCookie> foundProxyCookieOptional = cookieStorage.findCookie(proxyCookie.getUuid(), proxyCookie.getHost(), prefIdpsGoogleIdporten);
        Optional<ProxyCookie> foundExpiresSoonOptional = cookieStorage.findCookie(expiresSoon.getUuid(), expiresSoon.getHost(), prefIdpsIdportenGoogle);

        Date foundExpiresSoonExpiry = calculateDate(foundExpiresSoonOptional.get().getLastUpdated(), foundExpiresSoonOptional.get().getTouchPeriod());
        Date foundExpiresSoonMaxExpiry = calculateDate(foundExpiresSoonOptional.get().getCreated(), foundExpiresSoonOptional.get().getMaxExpiry());

        Date foundProxyCookieExpiry = calculateDate(foundProxyCookieOptional.get().getLastUpdated(), foundProxyCookieOptional.get().getTouchPeriod());
        Date foundProxyCookieMaxExpiry = calculateDate(foundProxyCookieOptional.get().getCreated(), foundProxyCookieOptional.get().getMaxExpiry());

        Assert.assertTrue(foundProxyCookieOptional.isPresent());
        Assert.assertTrue(foundExpiresSoonOptional.isPresent());

        Assert.assertEquals(proxyCookie.toString(), foundProxyCookieOptional.get().toString());
        Assert.assertEquals(expiresSoon.toString(), foundExpiresSoonOptional.get().toString());

        Assert.assertNotEquals(initialExpiresSoonExpiry, foundExpiresSoonExpiry);
        Assert.assertTrue(initialExpiresSoonExpiry.before(foundExpiresSoonExpiry));
        Assert.assertEquals(initialExpiresSoonMaxExpiry, foundExpiresSoonMaxExpiry);

        Assert.assertNotEquals(initialProxyCookieExpiry, foundProxyCookieExpiry);
        Assert.assertTrue(initialProxyCookieExpiry.before(foundProxyCookieExpiry));
        Assert.assertEquals(initialProxyCookieMaxExpiry, foundProxyCookieMaxExpiry);

    }


    @Test
    public void findValidCookies() throws Exception {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);

        Date now = new Date();
        
        String uuid = UUID.randomUUID().toString();

        // All cookies use same UUID and different idp
        ProxyCookie validCookie = new DefaultProxyCookie(uuid, "valid", "example.com", idpIdporten, security3, touchPeriod, maxExpiry, userDataIdporten);
        ProxyCookie expiredTouchCookie = new DefaultProxyCookie(uuid, "expired", "example.com", idpTwitter, security4, touchPeriod, maxExpiry, userDataTwitter,
                                                             calculateDate(now, -(touchPeriod + 1)), calculateDate(now, -(touchPeriod + 1)));
        ProxyCookie maxExpiredCookie = new DefaultProxyCookie(uuid, "maxExpired", "example.com", idpGoogle, security3, touchPeriod, maxExpiry, userDataGoogle,
                                                                                                  calculateDate(now, -(maxExpiry + 1)), now);

        // Asserting cookies has correct validity before inserting to database
        Assert.assertTrue(validCookie.isValid());
        Assert.assertFalse(expiredTouchCookie.isValid());
        Assert.assertFalse(maxExpiredCookie.isValid());

        // Inserting cookies to database
        ((DatabaseCookieStorage) cookieStorage).generateCookieInDb(validCookie);
        ((DatabaseCookieStorage) cookieStorage).generateCookieInDb(expiredTouchCookie);
        ((DatabaseCookieStorage) cookieStorage).generateCookieInDb(maxExpiredCookie);


        Optional<ProxyCookie> foundValidCookie = cookieStorage.findCookie(validCookie.getUuid(), validCookie.getHost(), prefIdpsGoogleTwitterIdporten);
        Optional<ProxyCookie> foundExpiredTouchCookie = cookieStorage.findCookie(expiredTouchCookie.getUuid(), expiredTouchCookie.getHost(), prefIdpsGoogleTwitterIdporten);
        Optional<ProxyCookie> foundMaxExpiredCookie = cookieStorage.findCookie(maxExpiredCookie.getUuid(), maxExpiredCookie.getHost(), prefIdpsGoogleTwitterIdporten);

        // Even though expiredTouchCookie and maxExpiredCookie isn't valid, a Google cookie should be found, as we're searching for cookies with
        // idp Google, Twitter and/or Idporten. That means a Google cookie will be found even though it's last on the preferred list.
        // In other words: All found cookies should be equal, exhibiting the same values of a the Google cookie except for lastUpdated
        Assert.assertTrue(foundValidCookie.isPresent());
        Assert.assertTrue(foundExpiredTouchCookie.isPresent());
        Assert.assertTrue(foundMaxExpiredCookie.isPresent());

        // Asserting that values in the cookies are in fact equal, except for lastUpdated
        Assert.assertEquals(foundValidCookie.get().getUuid(), foundExpiredTouchCookie.get().getUuid());
        Assert.assertEquals(foundExpiredTouchCookie.get().getUuid(), foundMaxExpiredCookie.get().getUuid());
        
        Assert.assertEquals(foundValidCookie.get().getName(), foundExpiredTouchCookie.get().getName());
        Assert.assertEquals(foundExpiredTouchCookie.get().getName(), foundMaxExpiredCookie.get().getName());

        Assert.assertEquals(foundValidCookie.get().getHost(), foundExpiredTouchCookie.get().getHost());
        Assert.assertEquals(foundExpiredTouchCookie.get().getHost(), foundMaxExpiredCookie.get().getHost());
        
        Assert.assertEquals(foundValidCookie.get().getIdp(), foundExpiredTouchCookie.get().getIdp());
        Assert.assertEquals(foundExpiredTouchCookie.get().getIdp(), foundMaxExpiredCookie.get().getIdp());

        Assert.assertEquals(foundValidCookie.get().getSecurity(), foundExpiredTouchCookie.get().getSecurity());
        Assert.assertEquals(foundExpiredTouchCookie.get().getSecurity(), foundMaxExpiredCookie.get().getSecurity());

        Assert.assertEquals(foundValidCookie.get().getTouchPeriod(), foundExpiredTouchCookie.get().getTouchPeriod());
        Assert.assertEquals(foundExpiredTouchCookie.get().getTouchPeriod(), foundMaxExpiredCookie.get().getTouchPeriod());

        Assert.assertEquals(foundValidCookie.get().getMaxExpiry(), foundExpiredTouchCookie.get().getMaxExpiry());
        Assert.assertEquals(foundExpiredTouchCookie.get().getMaxExpiry(), foundMaxExpiredCookie.get().getMaxExpiry());

        Assert.assertEquals(foundValidCookie.get().getCreated(), foundExpiredTouchCookie.get().getCreated());
        Assert.assertEquals(foundExpiredTouchCookie.get().getCreated(), foundMaxExpiredCookie.get().getCreated());

        Assert.assertEquals(foundValidCookie.get().getUserData(), foundExpiredTouchCookie.get().getUserData());
        Assert.assertEquals(foundExpiredTouchCookie.get().getUserData(), foundMaxExpiredCookie.get().getUserData());

        // Looking for cookie with valid uuid and idp, but wrong host
        Optional<ProxyCookie> foundCookieWrongHost = cookieStorage.findCookie(validCookie.getUuid(), "loremipsum.com", prefIdpsGoogleTwitterIdporten);

        // Looking for cookie with valid uuid and host, but wrong idp
        Optional<ProxyCookie> foundCookieWrongIdp = cookieStorage.findCookie(validCookie.getUuid(), "example.com", prefIdpsAmazonInstagram);

        // Looking for cookie with non-existing uuid
        Optional<ProxyCookie> foundNonExistingCookie = cookieStorage.findCookie("non-existing-uuid", "host.com", prefIdpsGoogleTwitterIdporten);

        Assert.assertFalse(foundCookieWrongHost.isPresent());
        Assert.assertFalse(foundCookieWrongIdp.isPresent());
        Assert.assertFalse(foundNonExistingCookie.isPresent());

    }

    @Test
    public void removeCookie() {
        CookieStorage cookieStorage = injector.getInstance(CookieStorage.class);

        String localhostUuid = UUID.randomUUID().toString();

        ProxyCookie localhostCookie = cookieStorage.generateCookieInDb(localhostUuid, "google-cookie", "localhost", idpGoogle, security3, touchPeriod, maxExpiry, userDataGoogle);
        ProxyCookie localhostCookie2 = cookieStorage.generateCookieInDb(localhostUuid, "idporten-cookie", "localhost", idpIdporten, security4, touchPeriod, maxExpiry, userDataIdporten);
        ProxyCookie difiCookie = cookieStorage.generateCookieInDb("idporten", "difi.no", idpIdporten, security4, touchPeriod, maxExpiry, userDataIdporten);

        Optional<ProxyCookie> foundLocalhostCookie = cookieStorage.findCookie(localhostCookie.getUuid(), localhostCookie.getHost(), prefIdpsGoogleIdporten);
        Optional<ProxyCookie> foundLocalhostCookie2 = cookieStorage.findCookie(localhostCookie2.getUuid(), localhostCookie2.getHost(), prefIdpsIdportenGoogle);
        Optional<ProxyCookie> foundDifiCookie = cookieStorage.findCookie(difiCookie.getUuid(), difiCookie.getHost(), prefIdpsIdportenGoogle);

        Assert.assertTrue(foundLocalhostCookie.isPresent());
        Assert.assertTrue(foundLocalhostCookie2.isPresent());

        // Try delete both localhost cookies with same uuid
        cookieStorage.removeCookie(localhostCookie.getUuid());

        Optional<ProxyCookie> foundLocalhostCookieAfterRemoval = cookieStorage.findCookie(localhostCookie.getUuid(), localhostCookie.getHost(), prefIdpsGoogleIdporten);
        Optional<ProxyCookie> foundLocalhostCookie2AfterRemoval = cookieStorage.findCookie(localhostCookie2.getUuid(), localhostCookie2.getHost(), prefIdpsIdportenGoogle);
        Optional<ProxyCookie> foundDifiCookieBeforeRemoval = cookieStorage.findCookie(difiCookie.getUuid(), difiCookie.getHost(), prefIdpsIdportenGoogle);

        Assert.assertFalse(foundLocalhostCookieAfterRemoval.isPresent());
        Assert.assertFalse(foundLocalhostCookie2AfterRemoval.isPresent());
        Assert.assertTrue(foundDifiCookieBeforeRemoval.isPresent());

        cookieStorage.removeCookie("non-existing-uuid"); // Removing non-existent cookie
        cookieStorage.removeCookie(difiCookie.getUuid());

        Optional<ProxyCookie> foundLocalhostCookieAfterTwoRemovals = cookieStorage.findCookie(localhostCookie.getUuid(), localhostCookie.getHost(), prefIdpsIdportenGoogle);
        Optional<ProxyCookie> foundDifiCookieAfterRemoval = cookieStorage.findCookie(difiCookie.getUuid(), difiCookie.getHost(), prefIdpsIdportenGoogle);

        Assert.assertFalse(foundLocalhostCookieAfterTwoRemovals.isPresent());
        Assert.assertFalse(foundDifiCookieAfterRemoval.isPresent());

    }


    private Date calculateDate(Date date, int minutes) {
        return new Date(date.getTime() + minutes * MINUTE);
    }
}

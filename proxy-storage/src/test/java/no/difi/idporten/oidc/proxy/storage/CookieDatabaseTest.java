package no.difi.idporten.oidc.proxy.storage;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static no.difi.idporten.oidc.proxy.storage.CookieDatabase.stringToMap;
import static no.difi.idporten.oidc.proxy.storage.CookieDatabase.mapToString;

public class CookieDatabaseTest {


    @BeforeMethod
    public void setUp() {
    }

    @Test
    public void testStringToMapSimple() {
        Map<String, String> userData = new HashMap<>();
        userData.put("pid", "11223355555");
        userData.put("otherId", "jonathan@spotify.com");

        String userDataString = mapToString(userData);

        Map<String, String> actual = stringToMap(userDataString);

        MatcherAssert.assertThat("UserData maps should be equal before and after stringifying",
                actual.entrySet(), Matchers.equalTo(userData.entrySet()));
    }

    @Test
    public void testStringToMapWithCommaOrCurlyBracesInUserData() {
        Map<String, String> userData = new HashMap<>();
        userData.put("pid", "11223355555");
        userData.put("otherId", "jonathan@spotify.com");
        userData.put("location", "Managua, Nicaragua");
        userData.put("strangeCharacters", "{{[[]$@£€==??]]}&&&");

        String userDataString = mapToString(userData);

        Map<String, String> actual = stringToMap(userDataString);

        MatcherAssert.assertThat("UserData maps should be equal before and after stringifying",
                actual.entrySet(), Matchers.equalTo(userData.entrySet()));
    }
}

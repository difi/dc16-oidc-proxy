package no.difi.idporten.oidc.proxy.proxy.util;

import org.apache.http.Header;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {
    public static String specificPathWithGoogle = "/google/a/specific/path";

    public static String unsecuredPath = "/unsecured";

    public static String contentOfASpecificPath = "content of a specific path";

    public static String contentOfAnUnsecuredPath = "content of an unsecured path";

    public static String mockServerHostName = "www.mockhost.com";

    public static String mockServerAddress = "http://localhost:8081";

    public static String cookieName = "PROXYCOOKIE";

    public static String googleApiPath = "/oauth2/v3/token";

    public static String googleLoginPath = "/o/oauth2/auth";

    public static String invalidCodeUrlSuffix = "?code=anInvalidCode";

    public static String idportenApiPath = "/idporten-oidc-provider/token";

    public static String idportenLoginPath = "/idporten-oidc-provider/authorize";

    public static String logoutPath = "/logout";

    public static String emailInGoogleToken = "vikfand@gmail.com";

    public static String subInGoogleToken = "108182803704140665355";

    public static String googleApiResponseContent = "{'access_token': 'ya29.Ci8dAzAibGLSpSo9h69_eve7JOskC49kmzhqg7E1fwZoSr6XA2B0y9V7ZGbt_FydMA','token_type': 'Bearer','expires_in': 3600,'refresh_token': '1/ZO_GEfsXflVCyiQUIIExKRSfCEnFsrTzwNsvcVQ56iI','id_token': 'eyJhbGciOiJSUzI1NiIsImtpZCI6IjE1MDgzMjdmODUzZGU4OTNlZWFhODZjMjAxNTI2ODk1ZDFlOTUxNDMifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiYXRfaGFzaCI6Ii16dzZmZlNXclo0LUVidnl0c0tkU1EiLCJhdWQiOiIxMDYzOTEwMjI0ODc3LWRocWQzNmMwOXNpdGY5YWxxM2piMHJmc2ZtZWJlMzVvLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTA4MTgyODAzNzA0MTQwNjY1MzU1IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF6cCI6IjEwNjM5MTAyMjQ4NzctZGhxZDM2YzA5c2l0ZjlhbHEzamIwcmZzZm1lYmUzNW8uYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJlbWFpbCI6InZpa2ZhbmRAZ21haWwuY29tIiwiaWF0IjoxNDY4MjI3ODAzLCJleHAiOjE0NjgyMzE0MDN9.BVxm1dJWX41H2FX8P6fnP2WjJyLP8T9LKVppTqRF2cclKPEj9jDafip55rdYEuxI0p_Hx0UKNLnLp8QWOrhLWLuuXhWzmcxYW-bAGk3AarokTbLcSNJagGXFILPjMDDJ-qaMBLJhSITPy3-VlXkBn98fznYljXIEBZ4hd0OD9c93pkJ_DKF2FZ2WeFZlolrv1LK9xZkw43QtCS1mVqdyLG8KJHqQh2VGdjCyF0y1te2E23A7yPq9zmiS_67YX9T_WhiZ24CqjWtlul9dpUuTRlhMoP9FovUdjJpg7ry9zQRQsIpqt5ijTVAUJ9xuepsH6ZrSOzHqYzBLBzEoTYw0sg'}";

    public static String idportenApiResponseContent = "{\n" +
            "  \"access_token\" : \"f6ce8020-7c2c-4cf0-992d-9d1ba7aa8ff2\",\n" +
            "  \"id_token\" : \"eyJqa3UiOiJodHRwczpcL1wvZWlkLWV4dHRlc3QuZGlmaS5ub1wvaWRwb3J0ZW4tb2lkYy1wcm92aWRlclwvandrIiwia2lkIjoiaWdiNUN5Rk1BbUZlZWk0TW5YQm82bWM5My03bUVwN29ncklxV2hNVGNLYyIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiIyTzMxSnA5RTRNdnNJNGRHaTU4YkZaTGY2dHB1IiwiYXVkIjoidGVzdF9ycF9laWRfZXh0dGVzdF9kaWZpIiwiYWNyIjoiTGV2ZWwzIiwiYW1yIjoiTWluaWQtUElOIiwiaXNzIjoiaHR0cHM6XC9cL2VpZC1leHR0ZXN0LmRpZmkubm9cL2lkcG9ydGVuLW9pZGMtcHJvdmlkZXJcLyIsInBpZCI6IjA4MDIzNTQ5OTMwIiwiZXhwIjoxNDY4OTE0MzAzLCJsb2NhbGUiOiJuYiIsImlhdCI6MTQ2ODkxMDcwM30.Mk2NBA-xCM1qtAHKSEbSFEpSFAhwdK5tWtQguxTjM4aHxi4qEhxJ6z1056vQIbnH3FWnXNZZlzhyDd4Tg5Fb5XQJNstwrTlkGQAEgul0YbTWgKLWoTXiId-kKP2YoXhn-DexAuleAmxVS6XtXoDqjd0HXGvx-JD_ufOzJ_YIr2U\",\n" +
            "  \"token_type\" : \"Bearer\",\n" +
            "  \"expires_in\" : 3599,\n" +
            "  \"refresh_token\" : \"90fc8152-3d1b-40dc-8eae-0114db775bcb\",\n" +
            "  \"scope\" : \"openid profile\"\n" +
            "}";

    public static Map<String, String> getHeadersAsMap(Header[] headers) {
        return Arrays.stream(headers)
                .collect(Collectors.toMap(Header::getName, Header::getValue));
    }
}

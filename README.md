# DifiCamp 2016 Open ID Connect Proxy project

[![Build Status](https://travis-ci.org/difi/dc16-oidc-proxy.svg?branch=master)](https://travis-ci.org/difi/dc16-oidc-proxy)
[![Codecov](https://codecov.io/gh/difi/dc16-oidc-proxy/branch/master/graph/badge.svg)](https://codecov.io/gh/difi/dc16-oidc-proxy)
[![Stories in Ready](https://badge.waffle.io/difi/dc16-oidc-proxy.png?label=ready&title=Ready)](https://waffle.io/difi/dc16-oidc-proxy)

## Oppgavetekst

Id-porten har implementert en «Proof of Concept» (PoC) for hvordan man kan ta i bruk Open ID Connect som teknologi for å håndtere integrasjonen mellom tjenester/applikasjoner og autentiseringsfunksjonen til id-porten.

Oppgaven går ut på å lage en proxy/mellomvare som basert på konfigurasjon beskytter underliggende ressurser ved å kreve autentisering via id-porten. Tanken er å illustrere hvordan man oppnår løse koblinger mellom applikasjon og id-porten som tjeneste. I praksis skjer dette ved at man kommuniserer med underliggende programvare ved å legge til http-elementer som inneholder de data man får fra id-porten. Web-applikasjonen som benytter proxyen forholder seg bare til om disse http-variablene eksisterer eller ikke, ikke hvordan eller hvorfor de er der.

En vil forhåpentlig illustrere hvordan man kan lage og bruke en slik proxy vha OIDC-grensesnittet som id-porten støtter, samt mulighetsrommet som oppstår når en sentralforvalter kan videreutvikle denne tjenesten. Eksempler på dette er å standardisere håndtering av applikasjoner som krever miks av nivå 3 og 4, samt hvordan vi kan potensielt få høyere sikkerhet på nye nivå 1 og 2 tjenester.

Dersom det er tid/kapasitet bør en utforske mulighetsrommet rundt å benytte denne til å fase ut dagens OpenAM installasjoner ved å utvikle en bro mellom denne proxy’en og integrasjon mot dagens SAML-protokoll. Formålet med dette er å sjekke om dette er en farbar vei for å sanere teknisk gjeld ved å kunne migrere dagens applikasjoner vekk fra OpenAM.

## Features

* Easy to create services that use Open ID connect authentication
* Let this proxy handle all authentication with identityProviders
* Receive user's data from multiple identityProviders in HTTP header

## Getting started

Include these dependencies in your pom.xml:

OIDC-PROXY
```xml
<dependency>
    <groupId>com.github.difi.dc16-oidc-proxy</groupId>
    <artifactId>proxy</artifactId>
    <version>0.7.0</version>
</dependency>
```

Google Guice
```xml
<dependency>
    <groupId>com.google.inject</groupId>
    <artifactId>guice</artifactId>
    <version>4.1.0</version>
</dependency>
```

Then include the JitPack repository in your pom.xml to build the OIDC-Proxy in your project:
It should be at the bottom of your pom.xml file, after <profiles>.

JitPack:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

###Create and set up your configuration file:

The configuration file consists of one or many hosts, one or many identity providers and some global variables. Only characters $ " { } [ ] : = , # ` ^ ? ! @ * & \ are forbidden unquoted, but we've quoted all strings for consistency.

Host:

This is how a host is configured.

```xml
host.hostname = {
  hostname: "hostname"                                         <!-- The domain name of the host -->
  backends: ["IP_address1", "IP_address2"]                     <!-- The IP addresses the server runs on -->
  cookie:                                                      <!-- All cookie attributes are optional in the host, but required globally in the file -->
    {
      name: "cookie_name",                                     <!-- If this host requires a cookie with other needs than the global cookie -->
      touchPeriod: 35,                                         <!-- Touch period is initial expiry in minutes, without -->
      maxExpiry: 120                                           <!-- Or higher max expiry -->
    }
  paths:                                                       <!-- Configured paths are secured paths -->
    [
      {
        path: "/login_to_idporten"                             <!-- Root path of the secured area -->
        security: 2                                            <!-- Level of security -->
        idp: "idporten"                                        <!-- What identity provider should be used to log in on the secured area -->
        redirect_uri: "redirect_uri"                           <!-- Optional in path, obligatory in idp -->
        scope: "scope"                                         <!-- Optional in path, obligatory in idp -->
      }
    ]
  preferred_idps: ["identity_provider1", "identity_provider2"] <!-- Preferred idps in descending order. A path's idp (if configured) will override this order, adding itself first -->
  logout_post_uri: "/logout"				                   <!-- The uri used to logout, triggering logout if user accesses url on host ending with this -->
  logout_redirect_uri: "http://localhost:8080/logout-this"     <!-- Optional. Where the client is redirected back to after removing cookie. Removes cookie and redirects to this address. Don't configure this if you want want your service to receive logout request on postUri with userData, just use postUri -->
  unsecured_paths: ["/unsecured_path1", "/unsecured_path2"]    <!-- Unsecured paths are paths that should not receive information about the user -->
}
```



Identity provider:

This is how an identity provider is configured

```xml
idp.identityProviderName = {
  class: "package.identityProvider.is.in.classname.of.identityProvider"    <!-- Which identity provider class should be used -->
  issuer: "issuer_url"                                                     <!-- The id/uri og the issuer. Used to validate the token-->
  api_uri: "api_uri"                                                       <!-- The uri where the api is found. used in the identity provider -->
  login_uri: "login_uri"                                                   <!-- The uri where the user is sent to log in -->
  jwk_uri: "jwk_uri"                                                       <!-- Where the JSONWebKey made by the authenticating service is located-->
  redirect_uri: "redirect_uri"                                             <!-- Where the identity provider should redirect back to. Configured in the identity provider-->
  client_id: "client_id"                                                   <!-- The client_id parameter used in the request to the identity provider -->
  password: "password"                                                     <!-- Password parameters used in the request towards the identity provider -->
  scope: "scope"                                                           <!-- The scope parameter used in the request towards the identity provider -->
  user_data_name: ["user_data_service_needs1", "user_data_service_needs2"] <!-- What user data collected from the log in should be sent to the service -->
  pass_along_data: "identifying_data_for_idp"                              <!-- Optional. If a user is logged into multiple idps on a host, server returns userData of first preferred idp with cookie and this additional data from other idps with cookie -->
  parameters: {                                                            <!-- Parameters have to be in the idp, but does not have to contain any parameters -->
    other_parameters: "like",                                              <!-- Other parameters need in the identity provider configuration -->
    security: 3
  }
}
```

Global:

These are global configurations which are required to be in the configuration file.

```xml
cookie.name = "global_cookie_name" <!-- Global name of the cookie the proxy uses -->
cookie.touchPeriod = 20            <!-- How long the cookies validity should be expanded every time used -->
cookie.maxExpiry = 60              <!-- How long the max expiry should be expanded every time used -->

logoutHeader = "X-Logout"          <!-- Optional variable in case the server wants to terminate cookie without user interaction. Set response HTTP header 'logoutHeader: true' -->
salt = "random_salt"               <!-- The salt is required globally in the conf-file -->
```

Other:

Lastly it is possible to configure what port the proxy should use.

```xml
listen = {
  port: 8080
}
```


Example configuration file with localhost:8080 as host and idporten as identity provider using ntnu.no as backend.
```xml
listen = {
  port: 8080
}

host.localhost = {
  hostname: "localhost:8080"
  backends: ["129.241.56.116"]
  cookie:
    {
      name: "localhost-cookie",
      touchPeriod: 20,
      maxExpiry: 60
    }
  paths:
    [
      {
        path: "/fakulteter"
        security: 3
        idp: "idporten"
      }
    ]
  preferred_idps: ["idporten", "identity_provider"]
  logout_post_uri: "/logout"
  logout_redirect_uri: "http://localhost:8080/logout-localhost"
  unsecured_paths: ["/studier", "/bilder"]
}

idp.idporten = {
  class: "no.difi.idporten.oidc.proxy.idp.IdportenIdentityProvider"
  issuer: "https://eid-exttest.difi.no/idporten-oidc-provider/"
  api_uri: "https://eid-exttest.difi.no/idporten-oidc-provider/token"
  login_uri: "https://eid-exttest.difi.no/idporten-oidc-provider/authorize"
  jwk_uri: "https://eid-exttest.difi.no/idporten-oidc-provider/jwk"
  redirect_uri: "http://localhost:8080/"
  client_id: "difi"
  password: "password"
  scope: "openid"
  user_data_name: ["pid", "sub"]
  pass_along_data: "pid"
  parameters: {
    security: 3,
    grant_type: "authorization_code"
  }
}

cookie.name = "proxy_cookie_name"
cookie.touchPeriod = 30
cookie.maxExpiry = 120

logoutHeader = "X-Logout"
salt = "2LMC539EF8nf04O9gndsfERGh3HI4ugjRTHnfAGmlwkSEhfnbi82finsdf"

```

###Set up the server with Guice:

Insert this code snippet into your main class to run the server with the configuration file.

```java
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;


Injector injector = Guice.createInjector(new ArrayList<Module>() {{
    add(new ConfigModule());
    add(new StorageModule());
    add(new ProxyModule());
}});

injector.getInstance(NettyHttpListener.class).run();
```

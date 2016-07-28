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


## Getting started

Include these dependencies in your pom.xml:`

OIDC-PROXY
```xml
<dependency>
    <groupId>com.github.difi.dc16-oidc-proxy</groupId>
    <artifactId>proxy</artifactId>
    <version>0.6.0</version>
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

The configuration file consists of one or many hosts, one or many identity providers and some global variables.

Host:

This is how a host is configured.

```xml
host.hostname = {
    hostname: hostname                                      <!-- The domain name of the host -->
    backends: [IP_address1, IP_address2]                    <!-- The IP addresses the server runs on -->
    cookie:                                                 <!-- All cookie attributes are optional in the host, but required globally in the file -->
    {               
    name: cookiename,                                       <!-- If this host requires a cookie with other needs than the global cookie -->
    touch: 35,                                              <!-- Example higher touch period -->
    maxExpiry: 120                                          <!-- Or higher max expiry -->
    }               
    paths:                                                  <!-- Configured paths are secured paths -->
    [{              
    path: /login_to_idporten                                <!-- Root path of the secured area -->
    security: 2                                             <!-- Level of security -->
    idp: idporten                                           <!-- What identity provider should be used to log in on the secured area-->
    redirect_uri: redirect_uri                              <!-- Optional in path -->
    scope: scope                                            <!-- Optional in path -->
    }]
    unsecured_paths: [/unsecured_path1, /unsecured_path2]   <!-- Unsecured paths are paths that should not receive information about the user-->
}
```



Identity provider:

This is how a identity provider is configured

```xml
idp.identityprovidername = {
    class: package.identityprovider.is.in.classname.of.identityprovider     <!-- Which idendtity provider class should be used -->
    client_id: client_id                                                    <!-- The client_id parameter used in the request to the identity provider-->
    redirect_uri: redirect_uri                                              <!-- Where the identity provider should redirect back to. Configured in the identity provider-->    
    password: password                                                      <!-- Password parameteres used in the request towards the identity provider -->
    scope: scope                                                            <!-- The scope parameter used in the request towrads the identity provider -->
    user_data_name: [user_data_service_needs1, user_data_service_needs2]    <!-- What user data collected from the log in should be sent to the service -->
    parameters: {                                                           <!-- Parameters have to be in the idp, but does not have to contain any parameters -->
        other_parameters: like,                                             <!-- Other parameteres need in the identity provider configuration-->
        security: 3
    }
}
```

Global:

These are global configurations which are required to be in the configuration file.

```xml
cookie.name = Global_proxy_name     <!-- Global name of the cookie the proxy uses -->
cookie.touch = 20                   <!-- How long the cookies validity should be expanded every time used -->
cookie.maxExpiry = 60               <!-- How long the max expiry should be expanded every time used-->

salt = random_salt                  <!-- The salt is required globally in the conf-file -->
```

Other:

Lastly it is possible to configure what port the proxy should use.

```xml
listen = {
    port: 8080
}
```


###Set up the server with Guice:

Insert this code snippet into your main class to run the server with the configuration file.

```java
        Injector injector = Guice.createInjector(new ArrayList<Module>() {{
            add(new ConfigModule());
            add(new StorageModule());
            add(new ProxyModule());
        }});

        injector.getInstance(NettyHttpListener.class).run();
```


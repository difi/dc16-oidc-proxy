# DifiCamp 2016 Open ID Connect Proxy project

[![Build Status](https://travis-ci.org/difi/dc16-oidc-proxy.svg?branch=master)](https://travis-ci.org/difi/dc16-oidc-proxy)
[![Codecov](https://codecov.io/gh/difi/dc16-oidc-proxy/branch/master/graph/badge.svg)](https://codecov.io/gh/difi/dc16-oidc-proxy)
[![Stories in Ready](https://badge.waffle.io/difi/dc16-oidc-proxy.png?label=ready&title=Ready)](https://waffle.io/difi/dc16-oidc-proxy)

## Oppgavetekst

Id-porten har implementert en «Proof of Concept» (PoC) for hvordan man kan ta i bruk Open ID Connect som teknologi for å håndtere integrasjonen mellom tjenester/applikasjoner og autentiseringsfunksjonen til id-porten.

Oppgaven går ut på å lage en proxy/mellomvare som basert på konfigurasjon beskytter underliggende ressurser ved å kreve autentisering via id-porten. Tanken er å illustrere hvordan man oppnår løse koblinger mellom applikasjon og id-porten som tjeneste. I praksis skjer dette ved at man kommuniserer med underliggende programvare ved å legge til http-elementer som inneholder de data man får fra id-porten. Web-applikasjonen som benytter proxyen forholder seg bare til om disse http-variablene eksisterer eller ikke, ikke hvordan eller hvorfor de er der.

En vil forhåpentlig ilustrere hvordan man kan lage og bruke en slik proxy vha OIDC-grensesnittet som id-porten støtter, samt mulighetsrommet som oppstår når en sentralforvalter kan videreutvikle denne tjenesten. Eksempler på dette er å standardisere håndtering av applikasjoner som krever miks av nivå 3 og 4, samt hvordan vi kan potensielt få høyere sikkerhet på nye nivå 1 og 2 tjenester.

Dersom det er tid/kapasitet bør en utforske mulighetsrommet rundt å benytte denne til å fase ut dagens OpenAM installasjoner ved å utvikle en bro mellom denne proxy’en og integrasjon mot dagens SAML-protokoll. Formålet med dette er å sjekke om dette er en farbar vei for å sanere teknisk gjeld ved å kunne migrere dagens applikasjoner vekk fra OpenAM.


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

###Create and set up your reference.conf-file:


Host:

```xml
host."hostname" = {
    hostname: "hostname"
    backends: ["IP-address1", "IP-address2"]
    cookie: { <!-- All cookie attributes are optional in the host, but required globally in the file -->
    name: "cookiename",
    touch: 20,
    maxExpiry: 60
    }
    paths: [
    {
    path: "/login_to_idporten"
    security: 2
    idp: "idporten"
    redirect_uri: "redirect_uri" <!-- Optional in path -->
    scope: "scope" <!-- Optional in path -->
    }
    ]
    unsecured_paths: ["/unsecured_path1", "/unsecured_path2"]
}
```



Identity provider:

```xml
idp."identityprovidername" = {
    class: "package.identityprovider.is.in.classname.of.identityprovider"
    client_id: "client_id"
    redirect_uri: "redirect_uri"
    password: "password"
    scope: "scope"
    user_data_name: ["user_data_service_needs1", "user_data_service_needs2"]
    parameters: { <!-- Parameters have to be in the idp, but does not have to contain any parameters -->
        other_parameters: "like:",
        security: 3
    }
}
```

Global:

```xml
cookie.name = Global_proxy_name
cookie.touch = 45
cookie.maxExpiry = 90

salt = random_salt <!-- The salt is required globally in the conf-file -->
```


###Set up the server with Guice:

```java
        Injector injector = Guice.createInjector(new ArrayList<Module>() {{
            add(new ConfigModule());
            add(new StorageModule());
            add(new ProxyModule());
        }});

        injector.getInstance(NettyHttpListener.class).run();
```


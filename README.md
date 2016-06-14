[![Stories in Ready](https://badge.waffle.io/difi/dc16-oidc-proxy.png?label=ready&title=Ready)](https://waffle.io/difi/dc16-oidc-proxy)
# DifiCamp 2016 Open ID Connect Proxy project

[![Build Status](https://travis-ci.org/difi/dc16-oidc-proxy.svg?branch=master)](https://travis-ci.org/difi/dc16-oidc-proxy)
[![Codecov](https://codecov.io/gh/difi/dc16-oidc-proxy/branch/master/graph/badge.svg)](https://codecov.io/gh/difi/dc16-oidc-proxy)

Id-porten har implementert en «Proof of Concept» (PoC) for hvordan man kan ta i bruk Open ID Connect som teknologi for å håndtere integrasjonen mellom tjenester/applikasjoner og autentiseringsfunksjonen til id-porten.

Oppgaven går ut på å lage en proxy/mellomvare som basert på konfigurasjon beskytter underliggende ressurser ved å kreve autentisering via id-porten. Tanken er å illustrere hvordan man oppnår løse koblinger mellom applikasjon og id-porten som tjeneste. I praksis skjer dette ved at man kommuniserer med underliggende programvare ved å legge til http-elementer som inneholder de data man får fra id-porten. Web-applikasjonen som benytter proxyen forholder seg bare til om disse http-variablene eksisterer eller ikke, ikke hvordan eller hvorfor de er der.

En vil forhåpentlig ilustrere hvordan man kan lage og bruke en slik proxy vha OIDC-grensesnittet som id-porten støtter, samt mulighetsrommet som oppstår når en sentralforvalter kan videreutvikle denne tjenesten. Eksempler på dette er å standardisere håndtering av applikasjoner som krever miks av nivå 3 og 4, samt hvordan vi kan potensielt få høyere sikkerhet på nye nivå 1 og 2 tjenester.

Dersom det er tid/kapasitet bør en utforske mulighetsrommet rundt å benytte denne til å fase ut dagens OpenAM installasjoner ved å utvikle en bro mellom denne proxy’en og integrasjon mot dagens SAML-protokoll. Formålet med dette er å sjekke om dette er en farbar vei for å sanere teknisk gjeld ved å kunne migrere dagens applikasjoner vekk fra OpenAM.

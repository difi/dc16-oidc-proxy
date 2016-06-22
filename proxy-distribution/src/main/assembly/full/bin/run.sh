#!/bin/sh

cd $(dirname $(readlink -f $0))/..

java -classpath .:lib/*:conf no.difi.idporten.oidc.proxy.dist.Main $@
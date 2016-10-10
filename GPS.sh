#!/bin/sh

java -Xss10m -jar build/libs/GPS.jar -f $1 -m $2

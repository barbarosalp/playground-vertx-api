#!/bin/bash

hey -c 50 -z 1m \
-T "application/json" \
-m POST \
-D ./helm/request.json \
http://localghost:30080/api/whiskies

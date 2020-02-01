#!/bin/bash

hey -c 30 -z 30s \
-T "application/json" \
-m POST \
-D ./helm/request.json \
http://localghost:30080/api/whiskies

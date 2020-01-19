#!/bin/bash

set -e

if [ -h "${BASH_SOURCE[0]}" ]; then
    WORK_DIR=$( cd "$( dirname "`readlink -f "${BASH_SOURCE[0]}"`" )" && pwd )
else
    WORK_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
fi

#mvn clean package
eval $(minikube docker-env)
docker build -t barb/vertx-api:1.0-SNAPSHOT .
#sudo -- sh -c "echo \\$(minikube ip) development.local >> /etc/hosts"

#https://github.com/jdolitsky/helm-servecm
#helm plugin install https://github.com/jdolitsky/helm-servecm
#helm repo add local http://127.0.0.1:8879/charts
#helm servecm --port=8879 --context-path=/charts --storage="local" --storage-local-rootdir="/Users/barbaros.alp/projects/barb/vertx-api/helm" & .

cd "${WORK_DIR}"/vertx-api
helm dependency update
cd ..

helm package "${WORK_DIR}"/vertx-api --destination "${WORK_DIR}"
helm repo index "${WORK_DIR}" --url http://127.0.0.1:8879/charts
helm repo update

cd "${WORK_DIR}"/component
helm dependency update
cd ..
helm package "${WORK_DIR}"/component --destination "${WORK_DIR}"

helm upgrade --install component  -f "${WORK_DIR}"/component-values.yaml "${WORK_DIR}"/component-1.0.0.tgz

kubectl rollout status --watch=true deployment component-vertx-api --timeout=60s

#!/bin/bash

set -e

if [ -h "${BASH_SOURCE[0]}" ]; then
    WORK_DIR=$( cd "$( dirname "`readlink -f "${BASH_SOURCE[0]}"`" )" && pwd )
else
    WORK_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
fi

# Variables
HELM_LOCAL_REPO="${HOME}"/.helm/localrepo

##https://github.com/jdolitsky/helm-servecm
##helm plugin install https://github.com/jdolitsky/helm-servecm
##helm repo add local http://127.0.0.1:8879/charts
##helm servecm --port=8879 --context-path=/charts --storage="local" --storage-local-rootdir="/Users/barbaros.alp/projects/barb/vertx-api/helm" &
#sudo -- sh -c "echo \\$(minikube ip) localghost >> /etc/hosts"

# Docker build
eval $(minikube docker-env)
docker build -t barb/vertx-api:1.0-SNAPSHOT .

# Application Chart
helm dependency update "${WORK_DIR}"/vertx-api
helm package "${WORK_DIR}"/vertx-api --destination "${HELM_LOCAL_REPO}"

# Add application chart to local helm repo
helm repo index "${HELM_LOCAL_REPO}" --url http://127.0.0.1:8879/charts
helm repo update

# Umbrella Chart
helm dependency update "${WORK_DIR}"/component
helm package "${WORK_DIR}"/component --destination "${HELM_LOCAL_REPO}"

# Install
helm upgrade --install component  -f "${WORK_DIR}"/component-values.yaml "${HELM_LOCAL_REPO}"/component-1.0.0.tgz

# Wait for healthcheck
kubectl rollout status --watch=true deployment component-vertx-api --timeout=60s

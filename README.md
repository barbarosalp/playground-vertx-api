# Setup

A simple vertx api along with the helm packages. Follow the steps below to build and deploy it.  

Benchmark [results](./helm/benchmark-result.txt).

## Prerequisites

Install `home brew`  
```shell
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
```

Install `hey` for benchmarking
```shell
brew install hey
```

### Kubernetes setup

Install `kubectl`
```shell
brew install kubectl
```

Install `minikube` 
```shell
brew install minikube
```

Install `helm`
```shell
brew install helm
```

Start the k8s cluster
```shell
minikube start --memory 8192 --cpus 6 --vm-driver=virtualbox
```

Enable metrics-server
```shell
minikube addons enable metrics-server
```

Add host entry for the minikube cluster
```shell
sudo -- sh -c "echo \\$(minikube ip) localghost >> /etc/hosts"
```

### Local helm repository setup
Install Helm Local repository web server
```shell
helm plugin install https://github.com/jdolitsky/helm-servecm
```

Start the local repo server
```shell
helm servecm --port=8879 --context-path=/charts --storage="local" --storage-local-rootdir="${HOME}/.helm/localrepo"
```

Add stable, incubator and @local repos.
```shell
helm repo add stable https://kubernetes-charts.storage.googleapis.com
helm repo add incubator https://kubernetes-charts-incubator.storage.googleapis.com/
helm repo add @local http://127.0.0.1:8879/charts
```

### Build, Deploy and Benchmark

From the root of the project:

- Deploy
```shell
./helm/deploy.sh
```

- Benchmark
```shell
./helm/benchmark.sh > ./helm/benchmark-result.txt
```

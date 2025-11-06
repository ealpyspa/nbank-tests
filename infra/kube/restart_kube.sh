#!/bin/bash

# Started local kubernetes cluster via minikube using Docker as driver
# cluster is running inside docker container
minikube start --driver=docker

# Created ConfigMap with "selenoid-config" map. File is available with key "browsers.json"
kubectl create configmap selenoid-config --from-file=browsers.json=./nbank-chart/files/browsers.json

# Install Helm chart with name of release "nbank" taking templates from ./nbank-chart
# this will create all resources described in helm chart (Deployment, Service) with parameters from values.yml
helm install nbank ./nbank-chart

# get all services in namespace=default
kubectl get svc

# get all pods in namespace=default
kubectl get pods

# Get log of concrete service
kubectl logs deployment/backend
kubectl logs deployment/frontend
kubectl logs deployment/selenoid
kubectl logs deployment/selenoid-ui

# Port-forward to local machine
kubectl port-forward svc/frontend 3000:80 # > /dev/null 2>&1 & (port-forward in background mode)
kubectl port-forward svc/backend 4111:4111
kubectl port-forward svc/selenoid 4444:4444
kubectl port-forward svc/selenoid-ui 8080:8080

#!/bin/bash

# STEP-1: Run application services
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

# STEP-2: Run monitoring services

helm repo add prometheus-community https://prometheus-community.github.io/helm-charts || true
helm repo add elastic https://helm.elastic.co || true
helm repo update

helm upgrade --install monitoring prometheus-community/kube-prometheus-stack -n monitoring --create-namespace -f monitoring-values.yaml

# Port forward to Prometheus and Grafana
kubectl port-forward svc/monitoring-kube-prometheus-prometheus -n monitoring 3001:9090
kubectl port-forward svc/monitoring-grafana -n monitoring 3002:80

# Create secrets for authorization on backend
kubectl create secret generic backend-basic-auth --from-literal=username=admin --from-literal=password=admin -n monitoring

# Apply yaml with spring-monitoring configuration (for backend)
kubectl apply -f spring-monitoring.yaml


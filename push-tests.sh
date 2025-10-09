#!/bin/bash

set -e

# Load secrets from .env
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
else
  echo ".env file not found!"
  exit 1
fi

# Check variables
if [ -z "$DOCKERHUB_USERNAME" ] || [ -z "$DOCKERHUB_TOKEN"]; then
  echo "DOCKERHUB_USERNAME password DOCKERHUB_TOKEN not set!"
  exit 1
fi

# Settings
IMAGE_NAME=nbank-tests
TAG=latest

# Login to Docker Hub with token
echo ">>> Login to Docker Hub with token"
echo $DOCKERHUB_TOKEN | docker login --username $DOCKERHUB_USERNAME --password-stdin

# Set tag for an image
echo ">>> Tagging an image"
docker tag $IMAGE_NAME $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG

# Push the image to Docker Hub
echo ">>> Pushing the image to Docker Hub"
docker push $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG

# Informational step
echo ">>> Done! The image is available via: docker pull $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"

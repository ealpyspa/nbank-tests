#!/bin/bash

echo ">>> Stop Docker Compose"
docker compose down

echo ">>> Docker pull of each browser image"

# File path (with browsers images)
json_file="./config/browsers.json"

# Check that jq is installed
if ! command -v jq &> /dev/null; then
    echo "❌ jq is not installed. Please install jq and try again."
    exit 1
fi

# Extract all values of .image with help of jq
images=$(jq -r '.. | objects | select(.image) | .image' "$json_file")

# Do docker pull for each browser image (in for cycle)
for image in $images; do
    echo "Pulling $image..."
    docker pull "$image"
done

echo ">>> Start Docker Compose"
docker compose up -d

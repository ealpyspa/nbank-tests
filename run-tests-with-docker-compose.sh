#!/bin/bash

# To run all tests: ./run-tests-with-docker-compose.sh
# To run api tests  ./run-tests-with-docker-compose.sh api
# To run ui tests   ./run-tests-with-docker-compose.sh ui

set -e  # Stop script if any command fails

# === SETTINGS ===
IMAGE_NAME=ealpyspa/nbank-tests:latest
TEST_PROFILE=${1:-api, ui}      # Default test profile = api and ui
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP
COMPOSE_FILE=./infra/docker_compose/docker-compose.yml
HOST_PWD=$(pwd -W 2>/dev/null || pwd)  #cross-platform version that works on both Linux and Windows automatically

# === PREPARE FOLDERS ===
mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"

echo ">>> Step 1. Start Docker Compose environment"
docker compose -f "$COMPOSE_FILE" up -d

echo ">>> Step 2. Run tests inside container"

docker run --rm \
  -v "$HOST_PWD/$TEST_OUTPUT_DIR/logs":/app/logs \
  -v "$HOST_PWD/$TEST_OUTPUT_DIR/results":/app/target/surefire-reports \
  -v "$HOST_PWD/$TEST_OUTPUT_DIR/report":/app/target/site \
  -e TEST_PROFILE="$TEST_PROFILE" \
  -e APIBASEURL=http://localhost:4111 \
  -e UIBASEURL=http://localhost:3000 \
  -e SELENOID_URL=http://localhost:4444 \
  -e SELENOID_UI_URL=http://localhost:8080 \
  $IMAGE_NAME

echo ">>> Step 3. Stop Docker compose environment"
docker compose -f "$COMPOSE_FILE" down

echo ">>> Step 4. Show results summary"
echo "Log file: $TEST_OUTPUT_DIR/logs/run.log"
echo "Tests results: $TEST_OUTPUT_DIR/results"
echo "Report: $TEST_OUTPUT_DIR/report"

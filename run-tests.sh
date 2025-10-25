#!/bin/bash

# Setting
IMAGE_NAME=nbank-tests
TEST_PROFILE=${1:-api} # run argument
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP

# Build Docker image
echo ">>> Build tests started"
docker build -t $IMAGE_NAME .

mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"

# Run Docker container
echo ">>> Tests are running"
docker run --rm \
  -v "$TEST_OUTPUT_DIR/logs":/app/logs \
  -v "$TEST_OUTPUT_DIR/results":/app/target/surefire-reports \
  -v "$TEST_OUTPUT_DIR/report":/app/target/site \
  -e TEST_PROFILE="$TEST_PROFILE" \
  -e APIBASEURL=http://94.41.189.137 \
  -e UIBASEURL=http://94.41.189.137 \
$IMAGE_NAME

# Results output
echo ">>> Tests are executed"
echo "Log file: $TEST_OUTPUT_DIR/logs/run.log"
echo "Tests results: $TEST_OUTPUT_DIR/results"
echo "Report: $TEST_OUTPUT_DIR/report"

# PowerShell script to build and run Docker tests on Windows

# --- Settings ---
$IMAGE_NAME = "nbank-tests"
$TEST_PROFILE = if ($args.Length -gt 0) { $args[0] } else { "api" }  # first argument or default "api"
$TIMESTAMP = Get-Date -Format "yyyyMMdd_HHmm"
$TEST_OUTPUT_DIR = "test-output/$TIMESTAMP"

# --- Build Docker image ---
Write-Host ">>> Build tests started"
docker build -t $IMAGE_NAME .

# --- Create local folders for logs and reports ---
New-Item -ItemType Directory -Force -Path "$TEST_OUTPUT_DIR/logs" | Out-Null
New-Item -ItemType Directory -Force -Path "$TEST_OUTPUT_DIR/results" | Out-Null
New-Item -ItemType Directory -Force -Path "$TEST_OUTPUT_DIR/report" | Out-Null

# --- Run Docker container ---
Write-Host ">>> Tests are running..."
docker run --rm `
  -v "${PWD}/$TEST_OUTPUT_DIR/logs:/app/logs" `
  -v "${PWD}/$TEST_OUTPUT_DIR/results:/app/target/surefire-reports" `
  -v "${PWD}/$TEST_OUTPUT_DIR/report:/app/target/site" `
  -e TEST_PROFILE="$TEST_PROFILE" `
  -e APIBASEURL="http://94.41.189.137" `
  -e UIBASEURL="http://94.41.189.137" `
  $IMAGE_NAME

# --- Results output ---
Write-Host ">>> Tests are executed"
Write-Host "Log file: $TEST_OUTPUT_DIR/logs/run.log"
Write-Host "Tests results: $TEST_OUTPUT_DIR/results"
Write-Host "Report: $TEST_OUTPUT_DIR/report"
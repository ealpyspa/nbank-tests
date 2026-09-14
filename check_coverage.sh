#!/bin/bash
set -e

THRESHOLD=50
HTML_FILE="allure-history/${GITHUB_RUN_NUMBER}/swagger-coverage-report.html"

if [[ ! -f "$HTML_FILE" ]]; then
  echo "❌ File not found: $HTML_FILE"
  exit 1
fi

echo "📄 Found HTML report: $HTML_FILE"
COVERAGE=$(grep -oP 'Full coverage:\s*\K[0-9]+([.,][0-9]+)?' "$HTML_FILE" | head -n 1 | tr ',' '.')

if [[ -z "$COVERAGE" ]]; then
  echo "❌ Could not extract coverage from HTML"
  exit 1
fi

echo "📊 Operations API Coverage: ${COVERAGE}% (threshold: ${THRESHOLD}%)"

if (( $(echo "$COVERAGE < $THRESHOLD" | bc -l) )); then
  echo "::warning::Operations API coverage is ${COVERAGE}%, below the ${THRESHOLD}% target"
else
  echo "✅ Quality gate passed!"
fi

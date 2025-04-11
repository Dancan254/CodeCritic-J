#!/bin/bash

# CodeCritic-J Release Script
echo "Packaging CodeCritic-J for release..."

# Clean and build
mvn clean package -DskipTests

# Create release directory
mkdir -p release
rm -rf release/*

# Copy required files
cp target/codecritic-j-*.jar release/
cp README.md release/
cp DOCUMENTATION.md release/
cp ARCHITECTURE.md release/
cp Dockerfile release/
cp docker-compose.yml release/
cp setup.sh release/
cp test-minimal-endpoint.sh release/
cp -r src/main/resources/samples release/samples

# Create a simple .env template
cat > release/.env.template << EOF
GITHUB_TOKEN=github_token_here
GITHUB_WEBHOOK_SECRET=webhook_secret_here
AZURE_AI_FOUNDRY_ENDPOINT=azure_endpoint_here
AZURE_AI_FOUNDRY_KEY=azure_key_here
AZURE_AI_FOUNDRY_DEPLOYMENT_ID=gpt-4
EOF

# Create a zip file
(cd release && zip -r ../codecritic-j-release.zip *)

echo "Release package created: codecritic-j-release.zip"
echo "Contains:"
echo "  - Java application jar"
echo "  - Docker and docker-compose files"
echo "  - Setup script"
echo "  - Documentation"
echo "  - Test scripts"
echo 
echo "To distribute:"
echo "  1. Share the zip file"
echo "  2. Recipients should run ./setup.sh after extracting" 

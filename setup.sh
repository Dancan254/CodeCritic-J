#!/bin/bash

# CodeCritic-J Setup Script
echo "Setting up CodeCritic-J..."

# Check if .env file exists
if [ -f .env ]; then
    echo ".env file already exists. Backing up to .env.bak"
    cp .env .env.bak
fi

# Prompt for GitHub Token
read -p "Enter your GitHub Token: " github_token
read -p "Enter your GitHub Webhook Secret (or press enter to generate one): " github_webhook_secret

# Generate webhook secret if not provided
if [ -z "$github_webhook_secret" ]; then
    github_webhook_secret=$(openssl rand -hex 32)
    echo "Generated webhook secret: $github_webhook_secret"
fi

# Prompt for Azure AI Foundry details
read -p "Enter your Azure AI Foundry Endpoint URL: " azure_endpoint
read -p "Enter your Azure AI Foundry API Key: " azure_key
read -p "Enter your Azure AI Foundry Deployment ID (default: gpt-4): " azure_deployment_id

# Use default value if not provided
if [ -z "$azure_deployment_id" ]; then
    azure_deployment_id="gpt-4"
fi

# Create .env file
cat > .env << EOF
GITHUB_TOKEN=$github_token
GITHUB_WEBHOOK_SECRET=$github_webhook_secret
AZURE_AI_FOUNDRY_ENDPOINT=$azure_endpoint
AZURE_AI_FOUNDRY_KEY=$azure_key
AZURE_AI_FOUNDRY_DEPLOYMENT_ID=$azure_deployment_id
EOF

chmod 600 .env

echo "Environment variables saved to .env file."
echo "To start the application:"
echo "  - Using Docker: docker-compose up -d"
echo "  - Without Docker: mvn spring-boot:run"
echo
echo "Remember to set up a webhook in your GitHub repository:"
echo "  - Webhook URL: http://your-server:8080/api/webhook/github"
echo "  - Content type: application/json"
echo "  - Secret: $github_webhook_secret"
echo "  - Events: Pull requests" 
# CodeCritic-J Documentation

## Table of Contents

1. [Introduction](#introduction)
2. [Installation](#installation)
   - [Prerequisites](#prerequisites)
   - [Quick Setup](#quick-setup)
   - [Manual Setup](#manual-setup)
3. [Configuration](#configuration)
   - [Environment Variables](#environment-variables)
   - [GitHub Setup](#github-setup)
   - [Azure AI Foundry Setup](#azure-ai-foundry-setup)
4. [Deployment Options](#deployment-options)
   - [Docker Deployment](#docker-deployment)
   - [Java Deployment](#java-deployment)
   - [Production Considerations](#production-considerations)
5. [Using CodeCritic-J](#using-codecritic-j)
   - [Creating a Pull Request](#creating-a-pull-request)
   - [Viewing Results](#viewing-results)
   - [Test Endpoint](#test-endpoint)
6. [Architecture](#architecture)
   - [Component Overview](#component-overview)
   - [Workflow Diagram](#workflow-diagram)
7. [Troubleshooting](#troubleshooting)
   - [Common Issues](#common-issues)
   - [Logs](#logs)
8. [Security Considerations](#security-considerations)
9. [Extending CodeCritic-J](#extending-codecritic-j)
   - [Adding Analysis Tools](#adding-analysis-tools)
   - [Customizing AI Prompts](#customizing-ai-prompts)
10. [FAQ](#faq)

## Introduction

CodeCritic-J is an AI-powered Java code review agent that integrates with GitHub to automatically analyze pull requests. When a pull request is opened or updated in a GitHub repository, CodeCritic-J:

1. Fetches the changed files
2. Runs static analysis tools (PMD and Checkstyle)
3. Performs AI-powered code review using Azure AI Foundry's GPT models
4. Posts the combined results as a comment on the pull request

This provides developers with immediate feedback on code quality, potential bugs, and adherence to best practices, enhancing the code review process.

## Installation

### Prerequisites

Before installing CodeCritic-J, ensure you have:

- **Java 17+**: Required for running the application
- **Maven**: For building from source (optional if using pre-built releases)
- **Docker & Docker Compose**: For containerized deployment (optional)
- **GitHub Account**: With permission to create tokens and set up webhooks
- **Azure AI Foundry Account**: With a GPT-4 deployment

### Quick Setup

1. Download and extract the release package:
   ```bash
   unzip codecritic-j-release.zip -d codecritic-j
   cd codecritic-j
   ```

2. Run the setup script:
   ```bash
   chmod +x setup.sh
   ./setup.sh
   ```

3. Follow the prompts to enter your GitHub and Azure credentials

4. Start the application using Docker:
   ```bash
   docker-compose up -d
   ```

### Manual Setup

If you prefer to set up without the script:

1. Create a `.env` file with your credentials:
   ```
   GITHUB_TOKEN=your_github_token
   GITHUB_WEBHOOK_SECRET=your_webhook_secret
   AZURE_AI_FOUNDRY_ENDPOINT=your_azure_endpoint
   AZURE_AI_FOUNDRY_KEY=your_azure_key
   AZURE_AI_FOUNDRY_DEPLOYMENT_ID=gpt-4
   ```

2. Start the application using your preferred method (see [Deployment Options](#deployment-options))

## Configuration

### Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `GITHUB_TOKEN` | GitHub Personal Access Token with `repo` scope | `ghp_1234abcd5678efgh...` |
| `GITHUB_WEBHOOK_SECRET` | Secret for validating webhook signatures | `6a6ddb5b35f85eedcc03b0f973e9ccf8...` |
| `AZURE_AI_FOUNDRY_ENDPOINT` | Azure AI Foundry service endpoint | `https://your-name.openai.azure.com` |
| `AZURE_AI_FOUNDRY_KEY` | Azure AI Foundry API key | `Ew7oLkTRuCq7og8YbxOjj2Lcvbk...` |
| `AZURE_AI_FOUNDRY_DEPLOYMENT_ID` | Azure AI Foundry deployment name | `gpt-4` |

### GitHub Setup

1. **Create a Personal Access Token (PAT)**:
   - Go to GitHub Settings → Developer settings → Personal access tokens → Fine-grained tokens
   - Create a new token with:
     - Repository access: Select repositories you want to analyze
     - Permissions: `pull requests` (read/write), `contents` (read)
   - Copy the token for your configuration

2. **Set up a Webhook** for each repository:
   - Go to the repository Settings → Webhooks → Add webhook
   - Payload URL: `http://your-server:8080/api/webhook/github`
   - Content type: `application/json`
   - Secret: Use the same value as `GITHUB_WEBHOOK_SECRET`
   - Events: Select "Pull requests" only
   - Active: Checked

### Azure AI Foundry Setup

1. **Create an Azure AI Foundry resource** in the Azure Portal
2. **Deploy a GPT-4 model** from the Azure AI Foundry resource:
   - Go to the "Deployments" section
   - Create a new deployment of GPT-4
   - Name it (this will be your `AZURE_AI_FOUNDRY_DEPLOYMENT_ID`)
3. **Get the endpoint and API key**:
   - In the "Keys and Endpoint" section, copy:
     - Endpoint URL (e.g., `https://your-name.openai.azure.com`)
     - One of the API keys

## Deployment Options

### Docker Deployment

The recommended deployment method:

```bash
# Start the application
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the application
docker-compose down
```

This automatically picks up environment variables from the `.env` file.

### Java Deployment

To run without Docker:

```bash
# Build the JAR (if not using a pre-built release)
mvn clean package

# Export environment variables
export GITHUB_TOKEN=your_github_token
export GITHUB_WEBHOOK_SECRET=your_webhook_secret
export AZURE_AI_FOUNDRY_ENDPOINT=your_azure_endpoint
export AZURE_AI_FOUNDRY_KEY=your_azure_key
export AZURE_AI_FOUNDRY_DEPLOYMENT_ID=gpt-4

# Run the application
java -jar codecritic-j-0.1.0-SNAPSHOT.jar
```

### Production Considerations

For production deployments:

- Use a reverse proxy (Nginx, Apache) for SSL termination
- Set up proper monitoring and restart policies
- Consider running behind a load balancer if handling many repositories
- Configure log rotation for long-term deployments

Example systemd service file:

```
[Unit]
Description=CodeCritic-J Service
After=network.target

[Service]
User=codecritic
WorkingDirectory=/opt/codecritic-j
EnvironmentFile=/opt/codecritic-j/.env
ExecStart=/usr/bin/java -jar /opt/codecritic-j/codecritic-j-0.1.0-SNAPSHOT.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

## Using CodeCritic-J

### Creating a Pull Request

Once set up, CodeCritic-J will automatically analyze any new pull request in the configured repositories:

1. Create or update a pull request in your repository
2. Wait a few moments for the analysis to complete
3. The analysis results will appear as a comment on the PR

### Viewing Results

The PR comment will contain:

- **Static Analysis Results**: Issues found by PMD and Checkstyle
- **AI Code Review**: Feedback from Azure AI Foundry, including:
  - Code quality assessment
  - Potential bugs and edge cases
  - Performance issues
  - Security concerns
  - Adherence to best practices

### Test Endpoint

To test your setup without creating a real PR:

```bash
# Run the test script
./test-minimal-endpoint.sh
```

This sends a simulated webhook event to the application's test endpoint, triggering the analysis process for a specific PR.

To customize the test:
1. Edit `src/main/resources/samples/minimal-pr-webhook.json`
2. Change the repository name and PR number to a real PR in your repository
3. Run the test script again

## Architecture

### Component Overview

CodeCritic-J consists of several key components:

1. **Webhook Controller**: Receives GitHub events and validates signatures
2. **GitHub Service**: Fetches PR details and posts comments
3. **PR Analysis Service**: Orchestrates the analysis workflow
4. **Static Analysis Service**: Runs PMD and Checkstyle on code
5. **AI Review Service**: Processes code with Azure AI Foundry
6. **Comment Service**: Formats and posts review results

### Workflow Diagram

```
GitHub PR Event → Webhook Controller → GitHub Service (Fetch PR) → PR Analysis Service
                                                                     ↓
                                            GitHub Service ← Format ← Analysis Results
                                            (Post Comment)            ↑
                                                                     / \
                                                         Static Analysis   AI Review Service
                                                            Service        (Azure AI Foundry)
```

## Troubleshooting

### Common Issues

1. **Webhook is not triggering analysis**
   - Check GitHub webhook settings and delivery logs
   - Verify webhook secret matches configuration
   - Ensure server is publicly accessible
   - Check application logs for errors

2. **"Resource not found" error in AI Review**
   - Verify Azure AI Foundry endpoint URL format (should be base URL only)
   - Check that deployment ID exactly matches the deployment name
   - Confirm the API key is valid
   - Try creating a new deployment or checking quotas

3. **GitHub API errors**
   - Ensure token has correct permissions
   - Check if token is expired or revoked
   - Verify repository name in webhook payload

### Logs

Default logs are available:

- **Docker**: `docker-compose logs -f`
- **Java**: Standard output or configured log file
- **Application**: Contains detailed information about each step

For more verbose logging, update `src/main/resources/application.properties`:
```
logging.level.com.codecritic=DEBUG
```

## Security Considerations

CodeCritic-J handles sensitive credentials and code. Best practices:

1. **Store credentials securely**:
   - Use a secure `.env` file with restricted permissions (`chmod 600 .env`)
   - Consider using a secret manager for production deployments

2. **Protect your webhook endpoint**:
   - Use a randomly generated webhook secret
   - Deploy behind SSL/TLS
   - Consider rate limiting or IP restrictions

3. **Limit GitHub token scope**:
   - Only grant access to required repositories
   - Use the minimum permissions needed

4. **Secure the test endpoint**:
   - Disable in production or restrict access
   - Don't expose it to public internet

## Extending CodeCritic-J

### Adding Analysis Tools

To add more static analysis tools:

1. Add the dependency to `pom.xml`
2. Create a new service class in `com.codecritic.service`
3. Integrate it with the `PRAnalysisService`

### Customizing AI Prompts

To modify the AI code review prompts:

1. Locate `AIReviewService.java`
2. Update the `CODE_REVIEW_PROMPT` constant
3. Modify the prompt construction in the `generateReview` method

Example of enhancing the prompt for security focus:
```java
private static final String CODE_REVIEW_PROMPT = 
    "You are CodeCritic, an expert Java security reviewer with deep knowledge of " +
    "OWASP top 10 vulnerabilities, secure coding practices, and common Java security pitfalls. " +
    "Review the following Java code diff and provide security-focused feedback.";
```

## FAQ

**Q: Can CodeCritic-J analyze languages other than Java?**
A: Currently, CodeCritic-J is optimized for Java code. However, the AI review can potentially analyze any code. Static analysis is Java-specific.

**Q: How much does it cost to run?**
A: Costs depend on your Azure AI Foundry usage. Each PR analysis consumes GPT-4 tokens based on the size of the code changes.

**Q: Can I run it on multiple repositories?**
A: Yes, you can set up webhooks on multiple repositories to use a single CodeCritic-J instance, as long as your GitHub token has access to all repositories.

**Q: Is my code sent to external services?**
A: Code changes are sent to Azure AI Foundry for analysis. No other external services receive your code.

**Q: How can I customize the analysis rules?**
A: PMD and Checkstyle rules can be customized in their respective configuration files in the `src/main/resources` directory. 
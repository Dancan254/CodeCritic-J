# CodeCritic-J

CodeCritic-J is an AI-powered Java code review agent built for the Azure Hackathon. It integrates with GitHub to automatically analyze pull requests, providing both static analysis feedback and AI-generated code reviews.

## Features

- **GitHub Integration**: Listens for pull request events via webhooks
- **Static Code Analysis**: Runs PMD and Checkstyle on Java code changes
- **AI Code Review**: Leverages Azure AI Foundry services to provide intelligent code review comments
- **Markdown Formatting**: Produces well-formatted code review comments with clear sections
- **Async Processing**: Handles reviews concurrently for better performance

## Architecture

CodeCritic-J is built using Spring Boot and leverages several key components:

1. **GitHub Webhook Processing**: Receives and validates GitHub events
2. **PR Analysis Service**: Coordinates static analysis and AI review
3. **Static Analysis Service**: Runs PMD and Checkstyle on code
4. **AI Review Service**: Uses Azure AI Foundry to generate intelligent reviews
5. **Comment Service**: Posts formatted reviews back to GitHub

## Setup Requirements

### Prerequisites

- Java 17 or higher
- Maven
- Azure AI Foundry API key (with GPT-4 deployment)
- GitHub Personal Access Token (PAT) with repo and admin:repo_hook scopes
- GitHub repository with webhook set up

### Environment Variables

The following environment variables need to be set:

```
GITHUB_TOKEN=your_github_pat
GITHUB_WEBHOOK_SECRET=your_webhook_secret
AZURE_AI_FOUNDRY_ENDPOINT=your_azure_ai_foundry_endpoint
AZURE_AI_FOUNDRY_KEY=your_azure_ai_foundry_key
AZURE_AI_FOUNDRY_DEPLOYMENT_ID=your_azure_ai_foundry_deployment_id
```

## Quick Setup

We provide a setup script to help you configure the application:

```bash
# Make the setup script executable
chmod +x setup.sh

# Run the setup script
./setup.sh
```

The script will:
1. Prompt for your GitHub token and optionally generate a webhook secret
2. Ask for your Azure AI Foundry details
3. Create a proper .env file
4. Provide instructions for starting the application

## Build & Run

### Using Maven

```bash
# Build the project
mvn clean package

# Run the application
java -jar target/codecritic-j-0.1.0-SNAPSHOT.jar
```

### Using Docker

```bash
# Build and run with docker-compose
docker-compose up -d
```

Or manually:

```bash
# Build Docker image
docker build -t codecritic-j .

# Run container
docker run -p 8080:8080 \
  -e GITHUB_TOKEN=your_github_pat \
  -e GITHUB_WEBHOOK_SECRET=your_webhook_secret \
  -e AZURE_AI_FOUNDRY_ENDPOINT=your_azure_ai_foundry_endpoint \
  -e AZURE_AI_FOUNDRY_KEY=your_azure_ai_foundry_key \
  -e AZURE_AI_FOUNDRY_DEPLOYMENT_ID=your_azure_ai_foundry_deployment_id \
  codecritic-j
```

## Setting Up GitHub Webhook

1. Go to your GitHub repository's settings
2. Select "Webhooks" and click "Add webhook"
3. Set Payload URL to `https://your-server:8080/api/webhook/github`
4. Set Content type to `application/json`
5. Set Secret to match your `GITHUB_WEBHOOK_SECRET`
6. Select "Let me select individual events" and choose "Pull requests"
7. Click "Add webhook"

## Testing Locally

For local testing, we provide a test script that simulates a GitHub webhook:

```bash
# Run the test script
./test-minimal-endpoint.sh
```

This will send a sample webhook payload to the local endpoint.

## Responsible AI Practices

CodeCritic-J implements several responsible AI practices:

- Clear attribution in comments that feedback is AI-generated
- Human-in-the-loop design (reviews are suggestions, not automatic changes)
- Transparency in the review process
- Secure handling of code and credentials

## Technologies Used

- **Spring Boot**: Web framework and dependency injection
- **Azure AI Foundry**: For intelligent code analysis and feedback using GPT-4
- **PMD & Checkstyle**: Static code analysis tools
- **GitHub API**: For repository and PR integration

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 
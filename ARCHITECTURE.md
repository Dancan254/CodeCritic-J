# CodeCritic-J Architecture Deep Dive

This document provides a comprehensive overview of the CodeCritic-J application architecture, from the controller layer to the repository layer.

## 1. Controller Layer - Entry Points

### WebhookController
- **Purpose**: Receives GitHub webhook events
- **Key Functions**:
  - Validates the webhook signature using `GITHUB_WEBHOOK_SECRET`
  - Extracts PR details from the payload (repository name, PR number)
  - Creates a `GitHubWebhookEvent` object
  - Delegates to the service layer for processing

### TestController
- **Purpose**: Provides test endpoints for local development
- **Key Functions**:
  - Accepts webhook payloads without validation
  - Useful for testing the application without exposing a public endpoint
  - Simulates GitHub webhook events

## 2. Service Layer - Core Business Logic

### GitHubWebhookService
- **Purpose**: Processes webhook events
- **Key Functions**:
  - Handles different event types (currently focusing on PR events)
  - Calls the GitHubService to fetch PR details
  - Delegates to the PRAnalysisService for analysis

### GitHubServiceImpl
- **Purpose**: Interacts with the GitHub API
- **Key Functions**:
  - Fetches PR details from GitHub using the GitHub API
  - Maps GitHub data to our domain models
  - Posts comments back to GitHub PRs
  - Contains fallback logic to create dummy PRs for testing when PRs aren't found

### PRAnalysisService
- **Purpose**: Orchestrates the PR analysis workflow
- **Key Functions**:
  - Runs static analysis on each file
  - Requests AI reviews for each file
  - Waits for all tasks to complete (using CompletableFuture)
  - Creates a consolidated review comment
  - Sends the comment back to GitHub

### StaticAnalysisService
- **Purpose**: Performs static code analysis
- **Key Functions**:
  - Runs PMD and Checkstyle on Java files
  - Converts tool-specific results to a unified format

### AIReviewService
- **Purpose**: Generates AI-powered code reviews
- **Key Functions**:
  - Formats code for AI review
  - Constructs prompts for the AI
  - Calls Azure AI Foundry API using the OpenAIClient
  - Parses and formats the AI response

### GitHubCommentService
- **Purpose**: Manages GitHub comment formatting and posting
- **Key Functions**:
  - Takes review content and formats it
  - Posts comments to the PR using GitHub API
  - Uses the PR's repository information to post to the correct repo

## 3. Model Layer - Domain Objects

### PullRequest
- Represents a GitHub pull request
- Contains PR metadata (ID, title, author, repository)
- Contains a list of ModifiedFile objects

### ModifiedFile
- Represents a file changed in a PR
- Contains file metadata and diff content
- Tracks change type (added, modified, deleted)

### AnalysisReport
- Contains static analysis results for a file
- Includes a list of AnalysisIssue objects

### AIReview
- Contains AI-generated code review for a file
- Stores the feedback text and metadata

### ReviewComment
- Represents a consolidated review to post to GitHub
- Combines static analysis and AI review results

### GitHubWebhookEvent
- Represents a webhook event from GitHub
- Contains event type, repository name, PR ID, etc.

## 4. External Integrations

### GitHub API Integration
- Uses `org.kohsuke.github` library
- Authenticates with GitHub using PAT
- Fetches PR details, file contents, and posts comments

### Azure AI Foundry Integration
- Uses `com.azure.ai.openai` client
- Sends code to Azure's GPT-4 model for analysis
- Processes responses into structured feedback

## Data Flow

1. **Webhook Reception**:
   - GitHub sends webhook → WebhookController validates → GitHubWebhookService processes

2. **PR Analysis**:
   - GitHubService fetches PR details → PRAnalysisService orchestrates analysis

3. **Parallel Processing**:
   - StaticAnalysisService analyzes code style and patterns
   - AIReviewService sends code to Azure AI Foundry for intelligent review

4. **Result Consolidation**:
   - PRAnalysisService waits for all analyses to complete
   - Creates a unified markdown-formatted comment

5. **Feedback Delivery**:
   - GitHubCommentService posts comment to the PR
   - Users see feedback directly in GitHub

## Key Technical Implementation Details

1. **Asynchronous Processing**:
   - Uses Spring's `@Async` annotation
   - Leverages `CompletableFuture` for parallel processing
   - Ensures efficient handling of multiple files

2. **Error Handling**:
   - Graceful fallback for non-existent PRs
   - Logging at appropriate levels
   - Try-catch blocks to prevent cascading failures

3. **Security**:
   - Webhook signature validation
   - Secure credential handling via environment variables
   - Limited scope GitHub tokens

4. **Extensibility**:
   - Modular design allows adding new analysis tools
   - Customizable AI prompts
   - Separate interfaces and implementations for testability

## Architecture Diagram

```
┌─────────────────┐        ┌─────────────────┐        ┌─────────────────┐
│  GitHub         │        │  CodeCritic-J   │        │  Azure          │
│  Repository     │        │  Application    │        │  AI Foundry     │
└────────┬────────┘        └────────┬────────┘        └────────┬────────┘
         │                          │                          │
         │                          │                          │
         │    1. PR Created         │                          │
         │------------------------->│                          │
         │                          │                          │
         │    2. Webhook Event      │                          │
         │------------------------->│                          │
         │                          │                          │
         │                          │  3. Fetch PR Details     │
         │<-------------------------│                          │
         │                          │                          │
         │                          │                          │
         │                          │  4. Send Code for        │
         │                          │     Analysis             │
         │                          │------------------------->│
         │                          │                          │
         │                          │  5. Return AI Review     │
         │                          │<-------------------------│
         │                          │                          │
         │    6. Post Comment       │                          │
         │<-------------------------│                          │
         │                          │                          │
```

This architecture allows the application to efficiently receive GitHub events, analyze code using both traditional static analysis and AI, and provide valuable feedback directly in pull requests, all while maintaining a clean separation of concerns. 
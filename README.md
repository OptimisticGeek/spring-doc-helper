# SpringDocHelper

[![Build](https://github.com/OptimisticGeek/spring-doc-helper/actions/workflows/build.yml/badge.svg)](https://github.com/OptimisticGeek/spring-doc-helper/actions/workflows/build.yml)
[![Dependabot Updates](https://github.com/OptimisticGeek/spring-doc-helper/actions/workflows/dependabot/dependabot-updates/badge.svg?branch=main)](https://github.com/OptimisticGeek/spring-doc-helper/actions/workflows/dependabot/dependabot-updates)
[![Release](https://badgen.net/github/release/OptimisticGeek/spring-doc-helper)](https://github.com/OptimisticGeek/spring-doc-helper/releases)
[![Stars](https://badgen.net/github/stars/OptimisticGeek/spring-doc-helper)](https://github.com/OptimisticGeek/spring-doc-helper/releases)
[![Version](https://img.shields.io/jetbrains/plugin/v/23730-springdochelper)](https://plugins.jetbrains.com/plugin/23730-springdochelper)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/23730-springdochelper)](https://plugins.jetbrains.com/plugin/23730-springdochelper)

<!-- Plugin description -->

SpringDocHelper is an IntelliJ IDEA plugin designed for Spring REST API documentation. It enables developers to quickly view, copy, and export API documentation in multiple formats (HTML, JSON, Markdown, cURL), generate test cases, and provides MCP Server support for AI assistants to interact with your Spring projects.

## Features

- **Mouse Hover Documentation** — Quick hints for class, field, response, params, and method
- **Smart Type Inference** — Object and generic return type inference
- **Gutter Icons** — HTTP method icons (GET, POST, PUT, DELETE) with copy operations and test case generation
- **Search Everywhere** — Double-shift to search APIs with wildcard, regex, case-sensitive, and HTTP method filtering
- **Swagger Support** — Swagger documentation parsing
- **Optimized Caching** — Reduced memory usage with efficient caching logic
- **MCP Service** — MCP Server for AI assistants to search and query API interfaces

## MCP Service

SpringDocHelper supports MCP (Model Context Protocol), allowing AI assistants (Claude, Cursor, Trae, etc.) to interact directly with your Spring projects.

### Configuration

Add the following configuration to your MCP-enabled AI tool:

```json
{
  "mcpServers": {
    "SpringDocHelper": {
      "url": "http://localhost:18080"
    }
  }
}
```

### Available Tools

After starting IDEA, the MCP service automatically starts on port 18080:

| Tool | Description |
|------|-------------|
| `getProjects` | Get a list of all open projects |
| `searchApiByKeyword` | Search APIs by keyword (URL, name, or remark) |
| `getApiForUrlAndMethod` | Get API details by URL and HTTP method |
| `getApiById` | Get complete API JSON Schema by Position ID |
| `generateCode` | Prompt template for generating client code |

### Usage Examples

AI assistants can help you through the MCP service:

- 🔍 Search for all API interfaces in your project
- 📋 Get detailed parameter and return value definitions
- 💻 Generate frontend calling code based on interface documentation
- 📚 Automatically generate API documentation

<!-- Plugin description end -->

## Installation

### Via IDE Plugin Marketplace

1. Open **Settings/Preferences** → **Plugins** → **Marketplace**
2. Search for **"SpringDocHelper"**
3. Click **Install**

### Manual Installation

1. Download the [latest release](https://github.com/OptimisticGeek/spring-doc-helper/releases/latest)
2. Open **Settings/Preferences** → **Plugins** → **⚙️** → **Install plugin from disk...**
3. Select the downloaded file

---

Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation

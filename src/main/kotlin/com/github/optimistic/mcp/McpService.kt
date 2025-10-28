package com.github.optimistic.mcp

import com.github.optimistic.editor.listener.toJsonSchema
import com.github.optimistic.spring.service.SpringApiService
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import open.ai.mcp.compiler.mcp
import open.ai.mcp.core.McpServer
import java.awt.datatransfer.StringSelection
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

private const val MIN_PORT = 18080
private const val MAX_PORT = 18100
private val usedPorts = ConcurrentHashMap<Int, Boolean>()

/**
 * MCP Server implementation for Spring Doc Helper
 * Provides tools, resources and prompts for working with Spring API documentation
 */
@Service(Service.Level.PROJECT)
class McpService(val project: Project) : McpServer(name = "SpringDocHelperMcp", version = "1.0.0") {
    var service = project.service<SpringApiService>()

    val port = findAvailablePort()
    var isStart = false

    /**
     * Tool: Search APIs by keyword
     * Allows the model to actively search for APIs based on keywords
     */
    @mcp.Tools(
        title = "Search APIs by keyword",
        description = "Fuzzy search APIs and return a list of matching interfaces"
    )
    fun searchApiByKeyword(keyword: String): String? {
        return ApplicationManager.getApplication().runReadAction<String> {
            var searchMethods = service.searchMethods(limit = 10) {
                return@searchMethods it.name.contains(keyword) || it.url.contains(keyword) || it.remark.contains(keyword)
            }
            if (searchMethods.isEmpty()) {
                return@runReadAction "No related APIs found"
            }
            searchMethods.stream().map { "url:${it.httpMethod} ${it.url} author:${it.author} remark:${it.remark} positionId:${it.position}" }.collect(Collectors.joining("\n\n"))
        }
    }

    /**
     * Tool: Get API by URL and Method
     * Allows the model to fetch specific API schema by URL and HTTP method
     */
    @mcp.Tools(
        title = "Get API by URL and Method",
        description = "Get the schema description of a specific API, including url, method, queryParams, requestBody, response, pathParams"
    )
    fun getApiForUrlAndMethod(url: String, method: String): String? {
        return ApplicationManager.getApplication().runReadAction<String> {
            var searchMethods = service.searchMethods(limit = 5) {
                return@searchMethods url.contains(it.url) && method.contains(it.httpMethod.name)
            }
            if (searchMethods.isEmpty()) {
                return@runReadAction "No related APIs found"
            }
            searchMethods.stream().map { it.toJsonSchema() }.collect(Collectors.joining("\n\n"))
        }
    }

    /**
     * Tool: Get API by ID
     * Allows the model to fetch specific API schema by its unique identifier
     */
    @mcp.Tools(
        title = "Get API by ID",
        description = "Get the complete schema description of an API by its unique identifier, including url, method, queryParams, requestBody, response, pathParams"
    )
    fun getApiById(apiId: String): String? {
        return ApplicationManager.getApplication().runReadAction<String> {
            var searchMethods = service.searchMethods(limit = 1) {
                return@searchMethods it.position == apiId
            }
            if (searchMethods.isEmpty()) {
                return@runReadAction "No related APIs found"
            }
            return@runReadAction searchMethods[0].toJsonSchema()
        }
    }

    /**
     * Prompt: Generate frontend code
     * Pre-built instruction template that tells the model to work with API documentation
     */
    @mcp.Prompt(name = "Generate frontend code", description = "Help users generate frontend code based on API documentation")
    fun generateCode(framework: String, language: String, apiDescription: String): String {
        return """
            Based on the following API description, generate frontend page and API request code using ${framework} in ${language}.
            
            API Description:
            ${apiDescription}
            
            Please generate complete frontend code implementation that meets the following requirements:
            1. Include complete request functions/services encapsulation
            2. Include type definitions for request parameters (if applicable)
            3. Include type definitions for response data (if applicable)
            4. Generate a complete frontend page component with reasonable UI layout and interactive design
            5. All fields must have detailed comments according to the API description
            6. Prioritize using existing component libraries in the project (such as Element Plus, Ant Design, etc.)
            7. Reuse existing functions and utility functions in the project
            8. Follow ${framework} and ${language} project code specifications and best practices
            9. Ensure consistency between the generated page and API code
            10. Add appropriate comments explaining business logic and usage
        """.trimIndent()
    }

    /**
     * Resource: System prompt and usage guidelines
     * Provides information about when and how to use this MCP service
     */
    @mcp.Resource(
        name = "MCP Service Usage Guidelines",
        description = "Guidelines on when and how to use this MCP service for API-related tasks",
        uri = "/aboutMe",
        mimeType = "text/plain"
    )
    fun getUsageGuidelines(): String {
        return """
            # SpringDocHelper MCP Service Usage Guidelines

            This MCP service provides tools for working with Spring API documentation. When you need to perform API-related tasks, 
            you should优先使用 these tools instead of other methods.

            ## When to Use This Service

            1. When the user needs to search for APIs in a Spring project
            2. When you need to get detailed information about specific APIs
            3. When generating frontend code based on API documentation

            ## Available Tools

            1. searchApiByKeyword - Use this to search for APIs by keyword
            2. getApiForUrlAndMethod - Use this to get API details by URL and HTTP method
            3. getApiById - Use this to get API details by its unique identifier
            4. generateCode - Use this to generate frontend code based on API documentation

            ## Best Practices

            1. Always use the most specific tool for the task at hand
            2. When searching for APIs, start with searchApiByKeyword
            3. When you have specific API information, use getApiById for the most accurate results
            4. For code generation tasks, use the generateCode prompt with appropriate parameters

            ## Example Usage

            When a user asks to find an API related to "user management", you should:
            1. Call searchApiByKeyword with keyword="user management"
            2. Analyze the results and determine the most relevant API
            3. If needed, get more details using getApiById with the positionId from the search results
            4. If the user wants to generate frontend code, use the generateCode prompt
        """.trimIndent()
    }

    suspend fun sse() {
        if (isStart) return
        showNotification(project, port)
        return sse(port, "localhost",)
    }


    private fun showNotification(project: Project, port: Int) {
        val notification = Notification(
            "SpringDocHelperNotification",
            "MCP Server Started",
            "SpringDocHelper MCP server is running on port $port\nProject: ${project.name}\n\n" +
                    "To configure this MCP server for Trae and other smart tools, use the configuration below:",
            NotificationType.INFORMATION
        )

        notification.addAction(NotificationAction.createSimple("Copy MCP Config") {
            copyMcpConfigToClipboard(port)
            notification.expire()
        })

        Notifications.Bus.notify(notification, project)
    }

    private fun copyMcpConfigToClipboard(port: Int) {
        val config = """
            {
              "mcpServers": {
                "SpringDocHelper": {
                  "url": "http://localhost:$port/mcp/sse"
                }
              }
            }
        """.trimIndent()

        CopyPasteManager.getInstance().setContents(StringSelection(config))
    }

    suspend fun stopSse() {
        stop().apply { isStart = false }
    }
}

// Unified success response format
data class MCPSuccessResponse(val success: Boolean = true, val data: Any?)

private fun findAvailablePort(): Int {
    for (port in MIN_PORT..MAX_PORT) {
        if (!usedPorts.containsKey(port) && isPortAvailable(port)) {
            usedPorts[port] = true
            return port
        }
    }
    return -1
}

/**
 * Check if port is available
 */
private fun isPortAvailable(port: Int): Boolean {
    return try {
        val socket = ServerSocket(port)
        socket.close()
        true
    } catch (e: Exception) {
        false
    }
}
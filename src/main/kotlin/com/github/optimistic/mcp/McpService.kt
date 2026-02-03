package com.github.optimistic.mcp

import com.github.optimistic.editor.listener.toJsonSchema
import com.github.optimistic.spring.service.SpringApiService
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import open.ai.mcp.compiler.mcp
import open.ai.mcp.core.McpServer
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.ServerSocket

/**
 * MCP Server implementation for Spring Doc Helper
 * Provides tools, resources and prompts for working with Spring API documentation
 * Spring Doc Helper 的 MCP 服务实现
 * 提供用于处理 Spring API 文档的工具、资源和提示词
 */
@Service(Service.Level.APP)
class McpService : McpServer(name = "SpringDocHelper", version = "1.1.1") {

    val port = 18080

    var isStart = false

    /**
     * Tool: Get all open projects
     * Allows the model to get a list of all currently open projects
     * 工具：获取所有打开的项目
     * 允许模型获取 IDE 中当前所有打开项目的列表
     */
    @mcp.Tools(
        title = "Get all open projects",
        description = "Get a list of all currently open projects in the IDEA."
    )
    fun getProjects(): String {
        return ApplicationManager.getApplication().runReadAction<String> {
            val projects = ProjectManager.getInstance().openProjects
            if (projects.isEmpty()) {
                "No open projects found."
            } else {
                projects.joinToString("\n") { it.name }
            }
        }
    }

    /**
     * Tool: Search APIs by keyword
     * Allows the model to actively search for APIs based on keywords
     * 工具：按关键字搜索 API
     * 允许模型根据关键字（url、名称或备注）模糊搜索 Spring REST API
     * 返回匹配的 API 列表，包含方法、URL、作者、备注和 PositionID
     * 支持可选的 project 参数用于过滤项目
     */
    @mcp.Tools(
        title = "Search APIs by keyword",
        description = "Fuzzy search Spring REST APIs by keyword (url, name, or remark). Returns a list of matching APIs with their Method, URL, Author, Remark and PositionID. Optionally filter by project."
    )
    fun searchApiByKeyword(keyword: String, project: String? = null): String? {
        return ApplicationManager.getApplication().runReadAction<String> {
            val projects = getTargetProjects(project)
            if (projects.isEmpty()) {
                return@runReadAction "No matching projects found."
            }

            val results = projects.flatMap { proj ->
                val service = proj.service<SpringApiService>()
                service.searchMethods(limit = 10) {
                    it.name.contains(keyword, true) || it.url.contains(keyword, true) || it.remark.contains(keyword, true)
                }.map { "project:${proj.name} url:${it.httpMethod} ${it.url} author:${it.author} remark:${it.remark} positionId:${it.position}" }
            }

            if (results.isEmpty()) {
                return@runReadAction "No related APIs found for keyword: $keyword"
            }
            results.joinToString("\n\n")
        }
    }

    /**
     * Tool: Get API by URL and Method
     * Allows the model to fetch specific API schema by URL and HTTP method
     * 工具：按 URL 和方法获取 API 详情
     * 根据 URL 和 HTTP 方法获取特定 API 的完整 JSON Schema 详情
     * 当已知端点路径时非常有用
     * 支持可选的 project 参数用于过滤项目
     */
    @mcp.Tools(
        title = "Get API details by URL and Method",
        description = "Get the full JSON Schema details for a specific API using its URL and HTTP method. Useful when you know the endpoint path. Optionally filter by project."
    )
    fun getApiForUrlAndMethod(url: String, method: String, project: String? = null): String? {
        return ApplicationManager.getApplication().runReadAction<String> {
            val projects = getTargetProjects(project)
            if (projects.isEmpty()) {
                return@runReadAction "No matching projects found."
            }

            val results = projects.flatMap { proj ->
                val service = proj.service<SpringApiService>()
                service.searchMethods(limit = 5) {
                    url.contains(it.url, true) && method.equals(it.httpMethod.name, true)
                }.map { it.toJsonSchema() }
            }

            if (results.isEmpty()) {
                return@runReadAction "No related APIs found for url: $url, method: $method"
            }
            results.joinToString("\n\n")
        }
    }

    /**
     * Tool: Get API by ID
     * Allows the model to fetch specific API schema by its unique identifier
     * 工具：按 ID 获取 API 详情
     * 使用唯一的 Position ID 获取特定 API 的完整 JSON Schema 详情
     * 这是检索 API 详情最精确的方式
     * 支持可选的 project 参数用于过滤项目
     */
    @mcp.Tools(
        title = "Get API details by ID",
        description = "Get the full JSON Schema details for a specific API using its unique Position ID. This is the most precise way to retrieve API details. Optionally filter by project."
    )
    fun getApiById(apiId: String, project: String? = null): String? {
        return ApplicationManager.getApplication().runReadAction<String> {
            val projects = getTargetProjects(project)
            if (projects.isEmpty()) {
                return@runReadAction "No matching projects found."
            }

            for (proj in projects) {
                val service = proj.service<SpringApiService>()
                val searchMethods = service.searchMethods(limit = 1) {
                    it.position == apiId
                }
                if (searchMethods.isNotEmpty()) {
                    return@runReadAction searchMethods[0].toJsonSchema()
                }
            }
            return@runReadAction "No related APIs found for id: $apiId"
        }
    }

    /**
     * Helper: Get target projects based on project name
     * 辅助方法：根据项目名称获取目标项目列表
     * 如果 projectName 为空，则返回所有打开的项目
     */
    private fun getTargetProjects(projectName: String?): List<Project> {
        val openProjects = ProjectManager.getInstance().openProjects.toList()
        if (projectName.isNullOrBlank()) {
            return openProjects
        }
        return openProjects.filter { it.name == projectName }
    }

    /**
     * Prompt: Generate client code
     * Adaptive prompt to generate code for any client platform (Web, Mobile, Desktop)
     * 提示词：生成客户端代码
     * 自适应提示词，用于为任何客户端平台（Web、Mobile、Desktop）生成代码
     */
    @mcp.Prompt(name = "Generate client code", description = "Generate production-ready client code (Web/Mobile/Desktop) for a specific API. Adaptive to project framework.")
    fun generateCode(framework: String, language: String, apiDescription: String): String {
        return """
            You are an expert full-stack developer capable of working with any client platform (Web, Mobile, Desktop).
            Your task is to generate production-ready client code based on the provided API description.
            
            **Critical Instruction**: 
            - **Context Analysis**: You MUST analyze the **current project context** to determine the platform, framework, and coding style.
              - For Web: Check `package.json`, `.vue`, `.tsx`, `angular.json`, etc.
              - For Flutter: Check `pubspec.yaml`, `.dart` files.
              - For iOS/Swift: Check `.xcodeproj`, `.swift` files.
              - For Android/Kotlin: Check `build.gradle`, `.kt` files.
            - **Adaptive Generation**: Ignore the suggested 'framework'/'language' parameters if they conflict with the actual project context. Match the existing project's patterns exactly.

            API Description:
            ${apiDescription}
            
            Requirements:
            1. **Type Safety**: Generate strong types/models for Requests and Responses (TypeScript interfaces, Dart classes, Swift structs, Kotlin data classes).
            2. **Network Layer**: Encapsulate API calls.
               - Use the project's existing networking library (e.g., Axios, Dio, Alamofire, Retrofit).
               - Handle serialization/deserialization.
            3. **UI Implementation**:
               - Generate the appropriate UI components (Widgets, Views, Components).
               - Follow the project's UI system (Material, Cupertino, AntD, or custom).
               - Include Loading, Error, and Validation states.
            4. **Code Quality**:
               - Add documentation (JSDoc, KDoc, Dart doc, etc.).
               - Follow language-specific idioms and linting rules.
            
            Output Format:
            Provide the code organized logically (e.g., Models, Service, View).
        """.trimIndent()
    }

    /**
     * Resource: System prompt and usage guidelines
     * Provides information about when and how to use this MCP service
     * 资源：系统提示词和使用指南
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

            This MCP service provides tools for working with Spring API documentation.
            
            ## Available Tools

            1. **getProjects**: Get a list of all open projects.
            2. **searchApiByKeyword**: Start here! Search for APIs by url, name, or remark. Supports optional project parameter.
            3. **getApiForUrlAndMethod**: Use this if you know the exact URL and Method. Supports optional project parameter.
            4. **getApiById**: Use this to get full details (JSON Schema) after finding an API via search. Supports optional project parameter.
            5. **generateCode**: Use this prompt to generate frontend code.

            ## Workflow Example

            1. User asks: "Create a page for user login."
            2. Call `searchApiByKeyword(keyword="login")`.
            3. Analyze results. Found `POST /api/login` with ID `com.example.AuthController#login`.
            4. Call `getApiById(apiId="com.example.AuthController#login")`.
            5. Use the returned JSON Schema and the `generateCode` prompt to create the React/Vue component.
        """.trimIndent()
    }

    suspend fun sse() {
        if (isStart) return
        isStart = true
        // Show notification on the first open project, or all?
        // Since it's app level, we can just notify. But we also want to export config.
//        exportMcpConfig(port)
        
        // Notify user
        val projects = ProjectManager.getInstance().openProjects
        if (projects.isNotEmpty()) {
            showNotification(projects[0], port)
        }
        
        return start(port)
    }

    /**
     * Export MCP config to user's .trae directory
     * 导出 MCP 配置到用户的 .trae 目录
     * 方便用户快速集成配置
     */
    private fun exportMcpConfig(port: Int) {
        try {
            val userHome = System.getProperty("user.home")
            val appDirs = listOf(
                "kimi" to "kimi",
                "trae" to ".trae",
                "cursor" to "cursor",
                "gemini" to "gemini",
                "codebuddy" to "codebuddy",
                ".claude" to ".claude",
                "codebuddycn" to "codebuddycn",
                ".trae-cn" to ".trae-cn"
            )
            val gson = GsonBuilder().setPrettyPrinting().create()
            val serverName = "SpringDocHelper"

            appDirs.forEach { (_, dirName) ->
                val appConfigDir = File(userHome, dirName)
                if (!appConfigDir.exists() || !appConfigDir.isDirectory) {
                    thisLogger().debug("Not Found Ide directory: $appConfigDir")
                    return@forEach
                }
                val configFile = File(appConfigDir, "mcp-servers.json")
                val rootObject = readMcpConfig(configFile)
                val mcpServersObject = rootObject.get("mcpServers")?.takeIf { it.isJsonObject }?.asJsonObject
                    ?: JsonObject()

                val serverConfig = JsonObject().apply {
                    addProperty("url", "http://localhost:$port/mcp/sse")
                }
                mcpServersObject.add(serverName, serverConfig)
                rootObject.add("mcpServers", mcpServersObject)

                configFile.writeText(gson.toJson(rootObject))
                thisLogger().debug("Export MCP config to: $configFile")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readMcpConfig(configFile: File): JsonObject {
        if (!configFile.exists()) {
            return JsonObject()
        }
        val content = configFile.readText().trim()
        if (content.isBlank()) {
            return JsonObject()
        }
        return try {
            val parsed = JsonParser.parseString(content)
            if (parsed.isJsonObject) parsed.asJsonObject else JsonObject()
        } catch (e: Exception) {
            JsonObject()
        }
    }


    private fun showNotification(project: Project, port: Int) {
        val notification = Notification(
            "SpringDocHelperNotification",
            "MCP Server Started",
            "SpringDocHelper MCP server is running on port $port\n" +
                    "Configuration has been exported to global MCP config files",
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
                  "url": "http://localhost:$port"
                }
              }
            }
        """.trimIndent()

        CopyPasteManager.getInstance().setContents(StringSelection(config))
    }

    suspend fun stopSse() {
        stop().apply { isStart = false }
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
}

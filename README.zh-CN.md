# SpringDocHelper

[![Build](https://github.com/OptimisticGeek/spring-doc-helper/actions/workflows/build.yml/badge.svg)](https://github.com/OptimisticGeek/spring-doc-helper/actions/workflows/build.yml)
[![Dependabot Updates](https://github.com/OptimisticGeek/spring-doc-helper/actions/workflows/dependabot/dependabot-updates/badge.svg?branch=main)](https://github.com/OptimisticGeek/spring-doc-helper/actions/workflows/dependabot/dependabot-updates)
[![Release](https://badgen.net/github/release/OptimisticGeek/spring-doc-helper)](https://github.com/OptimisticGeek/spring-doc-helper/releases)
[![Stars](https://badgen.net/github/stars/OptimisticGeek/spring-doc-helper)](https://github.com/OptimisticGeek/spring-doc-helper/releases)
[![Version](https://img.shields.io/jetbrains/plugin/v/23730-springdochelper)](https://plugins.jetbrains.com/plugin/23730-springdochelper)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/23730-springdochelper)](https://plugins.jetbrains.com/plugin/23730-springdochelper)

<!-- Plugin description -->

SpringDocHelper 是一款专为 Spring REST API 文档设计的 IntelliJ IDEA 插件。它帮助开发者快速查看、复制和导出多种格式的接口文档（HTML、JSON、Markdown、cURL），生成测试用例，并提供 MCP Server 支持，让 AI 助手能够与您的 Spring 项目进行交互。

## 功能特性

- **鼠标悬浮文档提示** — 快速查看类、字段、响应、参数和方法的文档
- **智能类型推断** — Object 与泛型返回值真实类型推断
- **侧边栏图标** — HTTP 方法图标（GET、POST、PUT、DELETE）支持复制操作和生成测试用例
- **全局搜索** — 双击 Shift 搜索接口，支持通配符、正则、区分大小写和 HTTP 方法过滤
- **Swagger 支持** — 支持 Swagger 文档解析
- **优化缓存** — 高效的缓存逻辑，减少内存占用
- **MCP 服务** — 提供 MCP Server，支持 AI 助手搜索和查询 API 接口

## MCP 服务

SpringDocHelper 支持 MCP（Model Context Protocol），允许 AI 助手（如 Claude、Cursor、Trae 等）直接与您的 Spring 项目交互。

### 配置方式

在支持 MCP 的 AI 工具中添加以下配置：

```json
{
  "mcpServers": {
    "SpringDocHelper": {
      "url": "http://localhost:18080"
    }
  }
}
```

### 可用工具

启动 IDEA 后，MCP 服务会自动在端口 18080 启动：

| 工具 | 说明 |
|------|------|
| `getProjects` | 获取所有打开的项目列表 |
| `searchApiByKeyword` | 按关键字搜索 API（支持 URL、名称、备注模糊搜索） |
| `getApiForUrlAndMethod` | 根据 URL 和 HTTP 方法获取 API 详情 |
| `getApiById` | 根据 Position ID 获取完整的 API JSON Schema |
| `generateCode` | 生成客户端代码的提示词模板 |

### 使用示例

AI 助手可以通过 MCP 服务帮您：

- 🔍 搜索项目中的所有 API 接口
- 📋 获取接口的详细参数和返回值定义
- 💻 根据接口文档生成前端调用代码
- 📚 自动生成接口文档
<!-- Plugin description end -->

## 安装方式

### 通过 IDE 插件市场

1. 打开 **设置/首选项** → **插件** → **市场**
2. 搜索 **"SpringDocHelper"**
3. 点击 **安装**

### 手动安装

1. 下载 [最新版本](https://github.com/OptimisticGeek/spring-doc-helper/releases/latest)
2. 打开 **设置/首选项** → **插件** → **⚙️** → **从磁盘安装插件...**
3. 选择下载的文件

---

基于 [IntelliJ Platform Plugin Template][template] 开发。

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation

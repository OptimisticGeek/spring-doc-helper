# springDocHelper

[![Release](https://badgen.net/github/release/OptimisticGeek/spring-doc-helper)](https://github.com/OptimisticGeek/spring-doc-helper/releases)
[![Version](https://img.shields.io/jetbrains/plugin/v/:23730-springdochelper)](https://plugins.jetbrains.com/plugin/23730-springdochelper)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/:23730-springdochelper)](https://plugins.jetbrains.com/plugin/23730-springdochelper)
  
<!-- Plugin description -->
This is a plugin for spring interface documentation. With this plugin, you can quickly view and copy interface documentation in HTML, JSON, Markdown, cURL, etc., and generate interface test cases for import into Yapi, Postman, etc.

这是一款针对spring的接口文档插件，通过本插件获取接口文档，快速查看接口文档，快速复制接口文档的html、json、markdown、cURL等，生成接口用例，导入到yapi、postman等。

## 支持功能
- 接口文档分析
- 鼠标悬浮文档提示（class、field、response、params、method）
- Object与泛型返回值真实类型推断
- 快速文档提示复制model的html、json
- GET|POST|PUT|DELETE左侧小图标，支持复制操作
- controller缓存逻辑

## Todo
- [ ] 基于接口的搜索
- [ ] 生成接口请求用例
- [ ] 针对指定类的自定义fields
- [ ] 导入到Yapi
- [ ] 导入到Postman
- [ ] 复制接口Curl请求
- [ ] swagger文档分析
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "spring-doc-helper"</kbd> >
  <kbd>Install</kbd>
  
- Manually:

  Download the [latest release](https://github.com/OptimisticGeek/spring-doc-helper/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>
---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation

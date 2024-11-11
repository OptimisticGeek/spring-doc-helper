<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# springDocHelper Changelog

## [Unreleased]

## [1.1.0] - 2024-11-12

### Added

- 通过注解判断Field是否为必填项

### Fixed

- 接口左侧小图标 - 数据未及时更新

## [1.0.7] - 2024-11-11

### Added

- 接口右键新增菜单`SpringDocHelper`，支持复制Curl、Json、JsonSchema到剪切板

### Changed

- 支持版本范围 232 ~ 243.*

## [1.0.6] - 2024-11-10

### Added

- 接口左侧小图标，支持复制JsonSchema

## [1.0.5] - 2024-11-8

### Changed

- 支持版本 232 ~ 242.*
- 优化快速文档展现方式，仅显示接口与接口内变量名相关的文档

## [1.0.4] - 2024-8-15

### Added

- intelliJPlatform版本为2.0

### Changed

- Idea支持范围为 232 ~ 242.*
- kotlin升级为2.0.10

## [1.0.3]

- 支持Idea 2024.1版本

## [1.0.2]

- fix - Idea索引构建完成后执行接口扫描
- fix - 项目构建失败后，未重新获取modules
- fix - Search EveryWhere结果上限为100条，更快加载

## [1.0.1]

- fix - Object对象fields解析失败

## [1.0.0]

- 重构代码，接入SpringMvc插件
- 项目启动预加载数据，更快的Search EveryWhere和LinkMarker显示
- 使用UserData，更快的响应速度，更小的内存消耗

## [0.1.4]

- 接口左侧小图标，支持创建接口测试操作，需要Idea支持[HttpClient](https://www.jetbrains.com/help/idea/2023.3/http-client-in-product-code-editor.html)
- 支持swagger文档解析

## [0.1.3]

- search everywhere加载更快速，支持正则、单词、大小写方式匹配，也支持*/空格模糊匹配
- fix - 修复搜索接口时，分页重复数据

## [0.1.2]

- 双击shift搜索接口（search everywhere），支持url+注释+作者模糊搜索，支持按http方法过滤，支持异步搜索
- Http请求方法小图标展示
- controller缓存逻辑优化
- fix - queryParams的fieldType是list时，未正常加载参数名

## [0.1.1]

- 复制接口Curl请求

## [0.1.0]

- 接口文档分析
- 鼠标悬浮文档提示（class、field、response、params、method）
- Object与泛型返回值真实类型推断
- 快速文档提示复制model的html、json
- GET|POST|PUT|DELETE左侧小图标，支持复制操作
- controller缓存逻辑

[Unreleased]: https://github.com/OptimisticGeek/spring-doc-helper/compare/v1.0.6...HEAD
[1.0.7]: https://github.com/OptimisticGeek/spring-doc-helper/compare/v1.0.6...v1.0.7
[1.0.6]: https://github.com/OptimisticGeek/spring-doc-helper/compare/v1.0.5...v1.0.6
[1.0.5]: https://github.com/OptimisticGeek/spring-doc-helper/compare/v1.0.4...v1.0.5
[1.0.4]: https://github.com/OptimisticGeek/spring-doc-helper/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/OptimisticGeek/spring-doc-helper/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/OptimisticGeek/spring-doc-helper/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/OptimisticGeek/spring-doc-helper/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/OptimisticGeek/spring-doc-helper/compare/v0.1.4...v1.0.0
[0.1.4]: https://github.com/OptimisticGeek/spring-doc-helper/compare/v0.1.3...v0.1.4
[0.1.3]: https://github.com/OptimisticGeek/spring-doc-helper/compare/v0.1.2...v0.1.3
[0.1.2]: https://github.com/OptimisticGeek/spring-doc-helper/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/OptimisticGeek/spring-doc-helper/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/OptimisticGeek/spring-doc-helper/commits/v0.1.0

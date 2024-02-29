<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# springDocHelper Changelog

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

## [Unreleased]
- [ ] 生成接口请求用例
- [ ] 针对指定类的自定义fields
- [ ] 导入到Yapi
- [ ] 导入到Postman

[Unreleased]: https://github.com/OptimisticGeek/spring-doc-helper/compare/v0.1.2...HEAD
[0.1.0]: https://github.com/OptimisticGeek/spring-doc-helper/releases/tag/v0.1.0
[0.1.1]: https://github.com/OptimisticGeek/spring-doc-helper/releases/tag/v0.1.1
[0.1.2]: https://github.com/OptimisticGeek/spring-doc-helper/releases/tag/v0.1.2
[0.1.3]: https://github.com/OptimisticGeek/spring-doc-helper/releases/tag/v0.1.3
[0.1.4]: https://github.com/OptimisticGeek/spring-doc-helper/releases/tag/v0.1.4
[1.0.0]: https://github.com/OptimisticGeek/spring-doc-helper/releases/tag/v1.0.0
[1.0.1]: https://github.com/OptimisticGeek/spring-doc-helper/releases/tag/v1.0.1

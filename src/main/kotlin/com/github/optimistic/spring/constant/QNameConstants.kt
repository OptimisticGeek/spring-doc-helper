// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimistic.spring.constant

const val REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping"
const val GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping"
const val POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping"
const val PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping"
const val DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping"
const val REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody"
const val PATH_VARIABLE = "org.springframework.web.bind.annotation.PathVariable"
const val REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam"
const val REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController"
const val CONTROLLER = "org.springframework.web.bind.annotation.Controller"
const val RESPONSE_BODY = "org.springframework.web.bind.annotation.ResponseBody"

const val RESOURCE = "javax.annotation.Resource"
const val AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired"
const val FASTJSON_JSON_FIELD = "com.alibaba.fastjson.annotation.JSONField"
const val JACKSON_JSON_FIELD = "com.fasterxml.jackson.annotation.JsonProperty"


const val NULLABLE = "javax.annotation.Nullable"
const val NULL = "javax.annotation.Null"
const val OBJECT = "java.lang.Object"
const val SWAGGER_API = "io.swagger.annotations.API"
const val SWAGGER_MODEL = "io.swagger.annotations.ApiModel"
const val SWAGGER_FIELD = "io.swagger.annotations.ApiModelProperty"
const val SWAGGER_METHOD = "io.swagger.annotations.ApiOperation"
const val SWAGGER_SCHEMA = "io.swagger.v3.oas.annotations.media.Schema"
const val SWAGGER_TAG = "io.swagger.v3.oas.annotations.tags.Tag"
const val SWAGGER_OPERATION = "io.swagger.v3.oas.annotations.Operation"
const val FORM_FILE = "org.springframework.web.multipart.MultipartFile"
const val BIG_DECIMAL = "java.math.BigDecimal"

// 表示字段必填的注解列表
val requiredAnnotations = setOf(
    "javax.validation.constraints.NotNull", // 表示字段不能为空
    "javax.validation.constraints.NotEmpty", // 表示字符串字段不能为空或空字符串
    "javax.validation.constraints.NotBlank", // 表示字符串字段不能为空或空白字符串
    "javax.validation.constraints.Min", // 表示数值字段的最小值
    "javax.validation.constraints.Max", // 表示数值字段的最大值
    "com.fasterxml.jackson.annotation.JsonProperty:required:true)", // Jackson 注解，表示该字段为必填项
    "com.google.gson.annotations.SerializedName:required:true", // Gson 注解，表示该字段为必填项
    "org.hibernate.validator.constraints.Length", // Hibernate Validator 注解，表示字段长度最小为1
    "javax.validation.constraints.AssertTrue", // 表示字段必须为 true
    "javax.validation.constraints.Past", // 表示字段必须是过去的日期
    "javax.validation.constraints.Future" // 表示字段必须是将来的日期
)

// 表示字段可选的注解列表
val optionalAnnotations = setOf(
    "javax.validation.constraints.Null", // 表示字段可以为空
    "javax.validation.constraints.Size:min:0", // 表示字符串字段可以有指定大小的范围
    "com.fasterxml.jackson.annotation.JsonProperty:required:false", // Jackson 注解，表示该字段为非必填项
    "com.google.gson.annotations.SerializedName:required:false", // Gson 注解，表示该字段为非必填项
    // "javax.validation.constraints.Email", // 表示字段可以是有效的邮箱格式，但并不强制
    "javax.validation.constraints.Pattern", // 表示字段可以匹配特定的正则表达式
    "javax.validation.constraints.Digits", // 表示数值的整数和小数位数限制
    "javax.validation.constraints.Positive", // 表示字段必须是一个正数
    "javax.validation.constraints.Negative", // 表示字段必须是一个负数
    "javax.validation.constraints.PositiveOrZero", // 表示字段必须是一个非负数
    "javax.validation.constraints.NegativeOrZero" // 表示字段必须是一个非正数
)

// 表示不序列化的注解列表
val nonSerializedAnnotations = setOf(
    "com.alibaba.fastjson.annotation.JSONField:serialize:false", // Fastjson 注解，表示该字段在序列化时可以被忽略
    "com.alibaba.fastjson.annotation.JSONField:deserialize:false", // Fastjson 注解，表示该字段在反序列化时可以被忽略
    "com.alibaba.fastjson2.annotation.JSONField:serialize:false", // Fastjson 注解，表示该字段在序列化时可以被忽略
    "com.alibaba.fastjson2.annotation.JSONField:deserialize:false", // Fastjson 注解，表示该字段在反序列化时可以被忽略
    "com.fasterxml.jackson.annotation.JsonIgnore", // Jackson 注解，表示该字段在序列化和反序列化时都被忽略
)
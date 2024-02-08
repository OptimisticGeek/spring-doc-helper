// Copyright 2023-2024 OptimisticGeek. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.optimisticgeek.spring.constant

object QNameConstants {
    val REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping"
    val GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping"
    val POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping"
    val PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping"
    val DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping"
    val REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody"
    val PATH_VARIABLE = "org.springframework.web.bind.annotation.PathVariable"
    val REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam"
    val REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController"
    val CONTROLLER = "org.springframework.web.bind.annotation.Controller"
    val RESPONSE_BODY = "org.springframework.web.bind.annotation.ResponseBody"

    val RESOURCE = "javax.annotation.Resource"
    val AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired"
    val FASTJSON_JSON_FIELD = "com.alibaba.fastjson.annotation.JSONField"
    val JACKSON_JSON_FIELD = "com.fasterxml.jackson.annotation.JsonProperty"


    val NULLABLE = "javax.annotation.Nullable"
    val NULL = "javax.annotation.Null"
    val OBJECT = "java.lang.Object"
    val SWAGGER_API = "io.swagger.annotations.API"
    val SWAGGER_MODEL = "io.swagger.annotations.ApiModel"
    val SWAGGER_FIELD = "io.swagger.annotations.ApiModelProperty"
    val SWAGGER_METHOD = "io.swagger.annotations.ApiOperation"
    val FORM_FILE = "org.springframework.web.multipart.MultipartFile"
    val BIG_DECIMAL = "java.math.BigDecimal"
}

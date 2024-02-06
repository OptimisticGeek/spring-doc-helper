package com.github.optimisticgeek.entity;

/**
 * ResultData
 */
public class AjaxResult<T> {

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    private T data;

    public AjaxResult(Integer code, String message, T data) {
        this.data = data;
        this.message = "OK";
        this.code = 0;
    }

    public AjaxResult(T data) {
        this.data = data;
        this.message = "OK";
        this.code = 0;
    }

    public AjaxResult() {
        this.message = "OK";
        this.code = 0;
    }

    public static AjaxResult success(T data) {
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.data = data;
        ajaxResult.message = "OK";
        ajaxResult.code = 0;
    }

    public static AjaxResult success1(T data) {
        return new AjaxResult(data);
    }
}
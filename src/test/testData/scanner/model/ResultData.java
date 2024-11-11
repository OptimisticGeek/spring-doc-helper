package com.github.optimistic.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ResultData
 */
@Getter
@Setter
@ToString
public class ResultData<T> {

    /**
     * 错误码
     */
    private Integer errCode;

    private String msg;

    private T data;
}
package com.github.optimistic.entity;

/**
 * ResultData
 */
@Getter
@Setter
@ToString
public class Pager<T> {

    /**
     * 总条数
     */
    private Integer total;

    /**
     * 当前页
     */
    private Integer page;

    /**
     * 数据集
     */
    private List<T> rows;
}
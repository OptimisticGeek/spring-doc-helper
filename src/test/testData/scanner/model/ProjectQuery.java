package com.github.optimistic.query;

/**
 * ResultData
 */
@Getter
@Setter
@ToString
public class ProjectQuery {

    /**
     * 总条数
     */
    private Integer total;

    /**
     * 当前页
     */
    private Integer page;

    /**
     * 商品信息
     */
    private String name;
}
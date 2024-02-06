package com.github.optimisticgeek.controller;

import com.github.optimisticgeek.entity.Pager;
import com.github.optimisticgeek.entity.AjaxResult;
import com.github.optimisticgeek.entity.ResultData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 项目Controller
 *
 * @param text 这是一段描述
 * @author OptimisticGeek
 * @date 2022-11-18
 */
@RestController
@RequestMapping("/project/project")
public class DerivationController {

    /**
     * 构造函数值传递
     */
    @GetMapping("/page")
    public AjaxResult test1() {
        if (!isShowAllProject()) {
            query.setCreateUserId(getUserId());
        }
        return new AjaxResult(1);
    }

    /**
     * 构造函数变量传递
     */
    @GetMapping("/page")
    public AjaxResult test2() {
        if (false) {
            query.setCreateUserId(getUserId());
        }
        List<Integer> list = new ArrayList<>();
        return new AjaxResult(list);
    }

    /**
     * 构造函数变量传递 + 干扰
     */
    @GetMapping("/page")
    public AjaxResult test3() {
        if (true) {
            return new AjaxResult();
        }
        int a = 0
        return new AjaxResult(a);
    }

    /**
     * 构造函数变量传递 + 三目选择 + 干扰
     */
    @GetMapping("/page")
    public AjaxResult test4() {
        if (!isShowAllProject()) {
            return new AjaxResult();
        }
        int a = 1
        return a > 0 ? new AjaxResult(a) : new AjaxResult();
    }

    /**
     * 构造函数变量传递 + 三目选择 + 干扰
     */
    @GetMapping("/page")
    public AjaxResult test5() {
        if (!isShowAllProject()) {
            return new AjaxResult();
        }
        int a = 1
        return a > 0 ? new AjaxResult(a) : new AjaxResult();
    }

    /**
     * 静态方法变量传递 + 三目选择 + 干扰
     */
    @GetMapping("/page")
    public AjaxResult test6() {
        if (!isShowAllProject()) {
            return new AjaxResult();
        }
        int a = 1
        return a > 0 ? AjaxResult.success(a) : new AjaxResult();
    }

    /**
     * 泛型
     */
    @GetMapping("/page")
    public ResultData<List<Integer>> test7() {
        return new ResultData();
    }

    /**
     * 通用泛型
     */
    @GetMapping("/page")
    public ResultData<?> test7() {
        return new ResultData(1);
    }

    /**
     * 变量
     */
    @GetMapping("/page")
    public ResultData<T> test8() {
        ResultData<T> result = new ResultData();
        result.data = new ArrayList<Integer>();
        return result;
    }

    /**
     * 变量
     */
    @GetMapping("/page")
    public ResultData<T> test9() {
        ResultData<T> result = new ResultData();
        result.setData(new ArrayList<ResultData<Integer>>());
        return result;
    }
    /**
     * 变量
     */
    @GetMapping("/page")
    public ResultData<T> test10() {
        return new ResultData(new ArrayList<ResultData<Integer>>());
    }
}
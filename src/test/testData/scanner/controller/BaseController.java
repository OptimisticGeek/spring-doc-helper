package com.github.optimisticgeek.controller;

import com.github.optimisticgeek.entity.Pager;
import com.github.optimisticgeek.entity.ResultData;
import com.github.optimisticgeek.query.ProjectQuery;
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
public class BaseController {

    /**
     * 统计项目数
     *
     * @param query 查询条件
     * @return 项目数
     */
    @GetMapping("/count")
    public ResultData<Map<String, Integer>> count(ProjectQuery query) {
        if (!isShowAllProject()) {
            query.setCreateUserId(getUserId());
        }
        return ResultData.success(projectService.countByQuery(query));
    }

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 项目数
     */
    @GetMapping("/page")
    public ResultData<Pager<Integer>> pager1(ProjectQuery query) {
        if (!isShowAllProject()) {
            query.setCreateUserId(getUserId());
        }
        return ResultData.success(projectService.countByQuery(query));
    }

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 项目数
     */
    @GetMapping("/page")
    public ResultData<Pager<List<Integer>>> pager2(ProjectQuery query) {
        if (!isShowAllProject()) {
            query.setCreateUserId(getUserId());
        }
        return ResultData.success(projectService.countByQuery(query));
    }

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 项目数
     */
    @GetMapping("/page")
    public Pager<Integer> pager3(ProjectQuery query) {
        if (!isShowAllProject()) {
            query.setCreateUserId(getUserId());
        }
        return ResultData.success(projectService.countByQuery(query));
    }


    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 项目数
     */
    @GetMapping("/page")
    public ResultData<Map<String, Map<String, Pager<List<Integer>>>>> pager4(ProjectQuery query) {
        if (!isShowAllProject()) {
            query.setCreateUserId(getUserId());
        }
        return ResultData.success(projectService.countByQuery(query));
    }

}
package com.lz.service;

import com.lz.pojo.dto.RegistrationAndAthleteDTO;
import com.lz.pojo.result.PageResult;
import com.lz.pojo.result.Result;

import java.util.Date;

/**
 * 注册服务
 * Created with IntelliJ IDEA.
 *
 * @author lz
 
 * @date 2023/12/06
 */
public interface RegistrationService {

    /**
     * 列表
     *
     * @param currentPage 当前页面
     * @param pageSize    页面大小
     * @param name        名字
     * @param status      地位
     * @param date        日期
     *
     * @return {@code PageResult}
     */
    PageResult list(int currentPage, int pageSize,
                                     String name, String status, Date date);

    /**
     * 运动员查询参赛记录列表
     *
     * @param currentPage  当前页面
     * @param pageSize     页面大小
     * @param name         名字
     * @param status       地位
     * @param stringToData 字符串到数据
     * @param id          uid
     *
     * @return {@code PageResult}
     */
    PageResult listByPlayer(int currentPage, int pageSize, 
                            String name, String status, Date stringToData, Long id);

    /**
     * 删除参赛记录
     *
     * @param id 编号
     * @return
     */
    Result<String> delete(Long id);

    /**
     * 同意参加
     *
     * @param id 编号
     */
    void attend(Long id);

    /**
     * 拒绝申请
     *
     * @param id 编号
     */
    void refuse(Long id);


    /**
     * 选择一项
     *
     * @param id 编号
     *
     * @return {@code RegistrationAndAthleteDTO}
     */
    RegistrationAndAthleteDTO selectOne(Long id);

    /**
     * 按运动员获取参赛记录总数
     *
     * @param id 编号
     *
     * @return int
     */
    int getRegistrationTotalByAthlete(Long id);
}
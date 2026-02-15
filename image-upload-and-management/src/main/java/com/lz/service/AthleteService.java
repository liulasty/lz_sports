package com.lz.service;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/29/16:56
 * @Description:
 */

import com.lz.Exception.NoAthleteException;
import com.lz.pojo.dto.AthleteDTO;
import com.lz.pojo.dto.AthleteUpdateDTO;
import com.lz.pojo.entity.Athlete;

/**
 * @author lz
 */
public interface AthleteService {
    

    /**
     * 运动员申请
     *
     * @return {@code String}
     */
    String add(AthleteDTO athleteDTO);

    /**
     * 查看申请
     *
     * @param id 编号
     *
     * @return {@code Athlete}
     */
    Athlete selectApply(Integer id) throws NoAthleteException;

    /**
     * 拒绝申请
     *
     * @param id 编号
     */
    void refusePlayer(Integer id);

    /**
     * 按用户 ID 删除
     *
     * @param id 编号
     */
    void deleteByUserId(Integer id);

    /**
     * 选择一项
     *
     * @param id 编号
     *
     * @return {@code Athlete}
     */
    Athlete selectOne(Integer id);

    /**
     * 更新
     *
     * @param id               编号
     * @param athleteUpdateDTO 运动员更新 DTO
     */
    void update(Integer id, AthleteUpdateDTO athleteUpdateDTO);
}
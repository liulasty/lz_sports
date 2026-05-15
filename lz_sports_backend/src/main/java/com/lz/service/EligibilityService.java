package com.lz.service;

import com.lz.dto.EligibilityConfigDTO;
import com.lz.dto.EligibilityPreviewVO;
import com.lz.eligibility.engine.EligibilityResult;

import java.util.List;

/**
 * 资格规则引擎编排服务。
 */
public interface EligibilityService {

    /**
     * 校验当前用户是否有资格报名指定项目。
     *
     * @param userId  当前用户 ID
     * @param eventId 赛事 ID
     * @param itemId  项目 ID
     * @return 校验结果
     */
    EligibilityResult check(Long userId, Long eventId, Long itemId);

    /**
     * 校验指定运动员是否有资格报名指定项目（管理员代查用）。
     *
     * @param athleteUserId 运动员用户 ID
     * @param eventId       赛事 ID
     * @param itemId        项目 ID
     * @return 校验结果
     */
    EligibilityResult checkByAthlete(Long athleteUserId, Long eventId, Long itemId);

    /**
     * 批量检查运动员在多个项目上的资格（项目列表灰显用）。
     *
     * @param userId  用户 ID
     * @param eventId 赛事 ID
     * @param itemIds 项目 ID 列表
     * @return 每个项目的资格结果
     */
    List<EligibilityPreviewVO> batchCheck(Long userId, Long eventId, List<Long> itemIds);

    /**
     * 获取指定项目的完整规则配置。
     */
    EligibilityConfigDTO getConfig(Long itemId);

    /**
     * 全量覆盖保存规则配置（独立事务，删旧插新）。
     *
     * @param itemId 项目 ID
     * @param dto    规则配置
     */
    void saveConfig(Long itemId, EligibilityConfigDTO dto);

    /**
     * 清空指定项目的所有规则（= 全体可报）。
     */
    void deleteConfig(Long itemId);
}

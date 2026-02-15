package com.lz.pojo.vo;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * VO项目
 *
 * @author lz
 * @date 2023/12/06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectVO implements Serializable {
    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 项目开始
     */
    private Date start;

    /**
     * 项目结束
     */
    private Date end;

    /**
     * 项目名字
     */
    private String name;
    
    private Long eventId;
    /**
     * 活动名称
     */
    private String eventName;

    /**
     * 参赛方式
     */
    private String state;

    /**
     * 角色为运动员时，是否参加该项目
     */
    private String isJoin;

    /**
     * 参赛年级
     */
    private String grade;

    /**
     * 参赛限制
     */
    private String limitation;

    /**
     * 项目开始
     */
    private Date projectStart;

    /**
     * 项目结束
     */
    private Date projectEnd;

    /**
     * 最大参赛人数
     */
    private Integer maxAttendance;
    /**
     * 已参赛参赛人数
     */
    private Integer attendance;
    
}
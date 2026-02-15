package com.lz.pojo.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/10/30/10:29
 * @Description:
 */

/**
 * @author lz
 */
@Data
@Builder
public class Event {
    /**
     * 事件 ID
     */
    @TableId(value = "eventId", type = IdType.AUTO)
    private Long eventId;
    /**
     * 活动名称
     */
    @TableField("EventName")
    private String eventName;
    /**
     * 注册开始
     */
    @TableField("RegistrationStart")
    private Date registrationStart;

    /**
     * 报名截止日期
     */
    @TableField("RegistrationEnd")
    private Date registrationDeadline;
    /**
     * 注册费
     */
    @TableField("RegistrationFee")
    private int registrationFee;
    /**
     * 资格
     */
    @TableField("Eligibility")
    private String eligibility;

    
    
}
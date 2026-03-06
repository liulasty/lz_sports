package com.lz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lz.common.enums.GenderLimit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Project Entity (Event Item)
 */
@TableName("event_item")
public class Project implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long itemId;

    @TableField("event_id")
    private Long eventId;

    @TableField("name")
    private String itemName;

    @TableField("create_time")
    private Date createTime;

    @TableField("grade_limit")
    private String grade;

    @TableField("gender_limit")
    private GenderLimit limitation;

    @TableField("max_count")
    private Integer maxAttendance;

    @TableField("current_count")
    private Integer attendance;

    @TableField("school_id")
    private Long schoolId;

    public Project() {}

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public GenderLimit getLimitation() {
        return limitation;
    }

    public void setLimitation(GenderLimit limitation) {
        this.limitation = limitation;
    }

    public Integer getMaxAttendance() {
        return maxAttendance;
    }

    public void setMaxAttendance(Integer maxAttendance) {
        this.maxAttendance = maxAttendance;
    }

    public Integer getAttendance() {
        return attendance;
    }

    public void setAttendance(Integer attendance) {
        this.attendance = attendance;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }
}

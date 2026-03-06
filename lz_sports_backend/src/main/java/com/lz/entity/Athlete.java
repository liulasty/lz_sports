package com.lz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lz.common.enums.AthleteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Athlete Entity
 */
@TableName("athlete")
public class Athlete implements Serializable {

    @TableId(value = "AthleteID", type = IdType.AUTO)
    private Long athleteId;

    @TableField(value = "UserID")
    private Long userId;

    @TableField(value = "Name")
    private String name;

    @TableField(value = "Age")
    private String age;

    @TableField(value = "Gender")
    private String gender;

    @TableField(value = "Contact")
    private String contact;

    @TableField(value = "AthleteState")
    private AthleteStatus athleteState;

    @TableField(value = "applyTime")
    private LocalDateTime applyTime;

    @TableField(value = "agreeTime")
    private LocalDateTime agreeTime;

    @TableField(value = "grade")
    private String grade;

    @TableField(value = "school_id")
    private Long schoolId;

    public Athlete() {}

    public Long getAthleteId() {
        return athleteId;
    }

    public void setAthleteId(Long athleteId) {
        this.athleteId = athleteId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public AthleteStatus getAthleteState() {
        return athleteState;
    }

    public void setAthleteState(AthleteStatus athleteState) {
        this.athleteState = athleteState;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
    }

    public LocalDateTime getAgreeTime() {
        return agreeTime;
    }

    public void setAgreeTime(LocalDateTime agreeTime) {
        this.agreeTime = agreeTime;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }
}

package com.lz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lz.common.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 赛事实体类
 */
@TableName("event")
public class Event implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long eventId;

    @TableField("name")
    private String eventName;

    @TableField("description")
    private String eventDescription;

    @TableField("reg_start_time")
    private Date registrationStartTime;

    @TableField("reg_deadline")
    private Date registrationEndTime;

    @TableField("start_time")
    private Date eventStartTime;

    @TableField("end_time")
    private Date eventEndTime;

    @TableField("status")
    private EventStatus eventStatus;

    @TableField("img_url")
    private String imageUrls;

    @TableField("school_id")
    private Long schoolId;
    
    public Event() {}

    public Event(Long eventId, String eventName, String eventDescription, Date registrationStartTime, Date registrationEndTime, Date eventStartTime, Date eventEndTime, EventStatus eventStatus, String imageUrls, Long schoolId) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.registrationStartTime = registrationStartTime;
        this.registrationEndTime = registrationEndTime;
        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;
        this.eventStatus = eventStatus;
        this.imageUrls = imageUrls;
        this.schoolId = schoolId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public Date getRegistrationStartTime() {
        return registrationStartTime;
    }

    public void setRegistrationStartTime(Date registrationStartTime) {
        this.registrationStartTime = registrationStartTime;
    }

    public Date getRegistrationEndTime() {
        return registrationEndTime;
    }

    public void setRegistrationEndTime(Date registrationEndTime) {
        this.registrationEndTime = registrationEndTime;
    }

    public Date getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(Date eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public Date getEventEndTime() {
        return eventEndTime;
    }

    public void setEventEndTime(Date eventEndTime) {
        this.eventEndTime = eventEndTime;
    }

    public EventStatus getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public static EventBuilder builder() {
        return new EventBuilder();
    }

    public static class EventBuilder {
        private Long eventId;
        private String eventName;
        private String eventDescription;
        private Date registrationStartTime;
        private Date registrationEndTime;
        private Date eventStartTime;
        private Date eventEndTime;
        private EventStatus eventStatus;
        private String imageUrls;
        private Long schoolId;

        public EventBuilder eventId(Long eventId) {
            this.eventId = eventId;
            return this;
        }

        public EventBuilder eventName(String eventName) {
            this.eventName = eventName;
            return this;
        }

        public EventBuilder eventDescription(String eventDescription) {
            this.eventDescription = eventDescription;
            return this;
        }

        public EventBuilder registrationStartTime(Date registrationStartTime) {
            this.registrationStartTime = registrationStartTime;
            return this;
        }

        public EventBuilder registrationEndTime(Date registrationEndTime) {
            this.registrationEndTime = registrationEndTime;
            return this;
        }

        public EventBuilder eventStartTime(Date eventStartTime) {
            this.eventStartTime = eventStartTime;
            return this;
        }

        public EventBuilder eventEndTime(Date eventEndTime) {
            this.eventEndTime = eventEndTime;
            return this;
        }

        public EventBuilder eventStatus(EventStatus eventStatus) {
            this.eventStatus = eventStatus;
            return this;
        }

        public EventBuilder imageUrls(String imageUrls) {
            this.imageUrls = imageUrls;
            return this;
        }

        public EventBuilder schoolId(Long schoolId) {
            this.schoolId = schoolId;
            return this;
        }

        public Event build() {
            return new Event(eventId, eventName, eventDescription, registrationStartTime, registrationEndTime, eventStartTime, eventEndTime, eventStatus, imageUrls, schoolId);
        }
    }
}

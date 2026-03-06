package com.lz.vo;

import com.lz.entity.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class ProjectVO extends Project {
    private String eventName;
    private String registrationStatus; // "已报名", "未报名", etc.

    public ProjectVO() {}

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }
}

package com.lz.vo;

import com.lz.entity.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectVO extends Project {
    private String eventName;
    private String registrationStatus; // "已报名", "未报名", etc.
}

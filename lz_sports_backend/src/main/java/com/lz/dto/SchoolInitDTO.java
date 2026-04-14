package com.lz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public class SchoolInitDTO {
    // School Info
    @NotBlank(message = "学校名称不能为空")
    private String schoolName;
    private String logoUrl;
    private String themeColor;
    @NotBlank(message = "联系邮箱不能为空")
    @Email(message = "联系邮箱格式不正确")
    private String contactEmail;

    // Admin Info
    @NotBlank(message = "管理员用户名不能为空")
    private String adminUsername;
    @NotBlank(message = "管理员密码不能为空")
    private String adminPassword;
    @NotBlank(message = "管理员邮箱不能为空")
    @Email(message = "管理员邮箱格式不正确")
    private String adminEmail;

    // Org Mode
    @Pattern(regexp = "^(?i)(UNIVERSITY|HIGH_SCHOOL)?$", message = "组织模式仅支持 UNIVERSITY 或 HIGH_SCHOOL")
    private String orgMode;

    // Grade Info
    @NotEmpty(message = "年级/院系列表不能为空")
    private List<String> grades;

    public SchoolInitDTO() {}

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getOrgMode() {
        return orgMode;
    }

    public void setOrgMode(String orgMode) {
        this.orgMode = orgMode;
    }

    public List<String> getGrades() {
        return grades;
    }

    public void setGrades(List<String> grades) {
        this.grades = grades;
    }
}

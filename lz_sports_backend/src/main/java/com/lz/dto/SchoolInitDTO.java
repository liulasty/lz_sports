package com.lz.dto;

import lombok.Data;
import java.util.List;

@Data
public class SchoolInitDTO {
    // School Info
    private String schoolName;
    private String logoUrl;
    private String themeColor;
    private String contactEmail;

    // Admin Info
    private String adminUsername;
    private String adminPassword;
    private String adminEmail;

    // Grade Info
    private List<String> grades;
}

package com.lz.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private String role;
    private String status;
    private String keyword;
    private Integer page = 1;
    private Integer size = 20;
}

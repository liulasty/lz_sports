package com.lz.vo;

import lombok.Data;
import java.util.List;

@Data
public class DepartmentTreeVO {
    private Long value; // 对应前端的 value
    private String label; // 对应前端的 label
    private String type; // 节点类型
    private List<DepartmentTreeVO> children;
}
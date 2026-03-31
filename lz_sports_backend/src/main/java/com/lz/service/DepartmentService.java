package com.lz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lz.entity.Department;
import com.lz.vo.DepartmentTreeVO;

import java.util.List;

public interface DepartmentService extends IService<Department> {
    
    /**
     * 获取部门结构（将宽表转为前端需要的级联/树形结构）
     * @return 部门树
     */
    List<DepartmentTreeVO> getDepartmentTree();
    
    /**
     * 获取指定部门名称拼接的字符串（用于导出）
     * @param deptId 部门ID
     * @return 例如：计算机学院-软件工程-1班 或 高一-1班
     */
    String getFullDepartmentName(Long deptId);
}

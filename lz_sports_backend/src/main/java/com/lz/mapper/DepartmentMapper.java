package com.lz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.entity.Department;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}

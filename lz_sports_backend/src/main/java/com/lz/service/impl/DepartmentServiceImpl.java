package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.entity.Department;
import com.lz.mapper.DepartmentMapper;
import com.lz.service.DepartmentService;
import com.lz.service.SchoolConfigService;
import com.lz.vo.DepartmentTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final SchoolConfigService schoolConfigService;

    @Override
    public List<DepartmentTreeVO> getDepartmentTree() {
        String orgMode = schoolConfigService.getCurrentOrgMode();
        List<Department> allDepts = this.list(new LambdaQueryWrapper<Department>()
                .eq(Department::getOrgMode, orgMode)
                .orderByAsc(Department::getSortOrder));
        
        List<DepartmentTreeVO> tree = new ArrayList<>();
        
        if ("K12".equals(orgMode)) {
            // K12模式：年级 -> 班级，或直接是独立部门
            Map<String, List<Department>> gradeMap = new LinkedHashMap<>();
            List<Department> independentDepts = new ArrayList<>();
            
            for (Department dept : allDepts) {
                if (dept.getGrade() != null && !dept.getGrade().isEmpty() && dept.getClassName() != null && !dept.getClassName().isEmpty()) {
                    gradeMap.computeIfAbsent(dept.getGrade(), k -> new ArrayList<>()).add(dept);
                } else if (dept.getDeptName() != null && !dept.getDeptName().isEmpty()) {
                    independentDepts.add(dept);
                }
            }
            
            long virtualId = -1L;
            for (Map.Entry<String, List<Department>> entry : gradeMap.entrySet()) {
                DepartmentTreeVO gradeNode = new DepartmentTreeVO();
                gradeNode.setValue(virtualId--);
                gradeNode.setLabel(entry.getKey());
                gradeNode.setType("GRADE");
                
                List<DepartmentTreeVO> classNodes = new ArrayList<>();
                for (Department dept : entry.getValue()) {
                    DepartmentTreeVO classNode = new DepartmentTreeVO();
                    classNode.setValue(dept.getId());
                    classNode.setLabel(dept.getClassName());
                    classNode.setType("CLASS");
                    classNodes.add(classNode);
                }
                gradeNode.setChildren(classNodes);
                tree.add(gradeNode);
            }
            
            for (Department dept : independentDepts) {
                DepartmentTreeVO node = new DepartmentTreeVO();
                node.setValue(dept.getId());
                node.setLabel(dept.getDeptName());
                node.setType("DEPARTMENT");
                tree.add(node);
            }
            
        } else {
            // UNIVERSITY模式：学院 -> 专业 -> 班级，或独立部门
            Map<String, Map<String, List<Department>>> collegeMap = new LinkedHashMap<>();
            List<Department> independentDepts = new ArrayList<>();
            
            for (Department dept : allDepts) {
                if (dept.getCollege() != null && !dept.getCollege().isEmpty() && dept.getClassName() != null && !dept.getClassName().isEmpty()) {
                    collegeMap.computeIfAbsent(dept.getCollege(), k -> new LinkedHashMap<>())
                             .computeIfAbsent(dept.getMajor() != null && !dept.getMajor().isEmpty() ? dept.getMajor() : "无专业", k -> new ArrayList<>())
                             .add(dept);
                } else if (dept.getDeptName() != null && !dept.getDeptName().isEmpty()) {
                    independentDepts.add(dept);
                }
            }
            
            long virtualId = -1L;
            for (Map.Entry<String, Map<String, List<Department>>> collegeEntry : collegeMap.entrySet()) {
                DepartmentTreeVO collegeNode = new DepartmentTreeVO();
                collegeNode.setValue(virtualId--);
                collegeNode.setLabel(collegeEntry.getKey());
                collegeNode.setType("COLLEGE");
                
                List<DepartmentTreeVO> majorNodes = new ArrayList<>();
                for (Map.Entry<String, List<Department>> majorEntry : collegeEntry.getValue().entrySet()) {
                    DepartmentTreeVO majorNode = new DepartmentTreeVO();
                    majorNode.setValue(virtualId--);
                    majorNode.setLabel(majorEntry.getKey());
                    majorNode.setType("MAJOR");
                    
                    List<DepartmentTreeVO> classNodes = new ArrayList<>();
                    for (Department dept : majorEntry.getValue()) {
                        DepartmentTreeVO classNode = new DepartmentTreeVO();
                        classNode.setValue(dept.getId());
                        classNode.setLabel(dept.getClassName());
                        classNode.setType("CLASS");
                        classNodes.add(classNode);
                    }
                    majorNode.setChildren(classNodes);
                    majorNodes.add(majorNode);
                }
                collegeNode.setChildren(majorNodes);
                tree.add(collegeNode);
            }
            
            for (Department dept : independentDepts) {
                DepartmentTreeVO node = new DepartmentTreeVO();
                node.setValue(dept.getId());
                node.setLabel(dept.getDeptName());
                node.setType("DEPARTMENT");
                tree.add(node);
            }
        }
        
        return tree;
    }

    @Override
    public String getFullDepartmentName(Long deptId) {
        if (deptId == null) {
            return "";
        }
        Department dept = this.getById(deptId);
        if (dept == null) {
            return "";
        }
        
        String orgMode = dept.getOrgMode();
        if (orgMode == null || orgMode.isEmpty()) {
            orgMode = schoolConfigService.getCurrentOrgMode();
        }
        List<String> names = new ArrayList<>();
        
        if ("K12".equals(orgMode)) {
            if (dept.getGrade() != null && !dept.getGrade().isEmpty() && dept.getClassName() != null && !dept.getClassName().isEmpty()) {
                names.add(dept.getGrade());
                names.add(dept.getClassName());
                return String.join("-", names);
            }
        } else {
            if (dept.getCollege() != null && !dept.getCollege().isEmpty() && dept.getClassName() != null && !dept.getClassName().isEmpty()) {
                names.add(dept.getCollege());
                if (dept.getMajor() != null && !dept.getMajor().equals("无专业") && !dept.getMajor().isEmpty()) {
                    names.add(dept.getMajor());
                }
                names.add(dept.getClassName());
                return String.join("-", names);
            }
        }
        
        if (dept.getDeptName() != null && !dept.getDeptName().isEmpty()) {
            return dept.getDeptName();
        }
        
        return "";
    }
}

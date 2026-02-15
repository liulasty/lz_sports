package com.lz.controller;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lz.Dao.AthleteDao;
import com.lz.Exception.MyException;
import com.lz.context.BaseContext;
import com.lz.pojo.dto.EventListDto;
import com.lz.pojo.dto.JoinProjectDTO;
import com.lz.pojo.dto.ProjectDTO;
import com.lz.pojo.entity.Athlete;
import com.lz.pojo.entity.Project;
import com.lz.pojo.result.PageResult;
import com.lz.pojo.result.Result;
import com.lz.pojo.vo.ProjectVO;
import com.lz.service.ImgService;
import com.lz.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

/**
 * @author lz
 */
@Slf4j
@RestController
@RequestMapping("sports/project")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;

    @Autowired
    private AthleteDao athleteDao;
    
    
    
    @GetMapping("/page")
    public Result<PageResult> list(@RequestParam(required = false) String name,
                                   @RequestParam(required = false) String event,
                                   @RequestParam(required = false) String date,
                                   @RequestParam(defaultValue = "1") int currentPage,
                                   @RequestParam(defaultValue = "5") int pageSize) {

        log.info("分页查询:{},{},{}", currentPage,pageSize,event);
        int offset = (currentPage - 1) * pageSize;
        Long uid = BaseContext.getCurrentId();
        QueryWrapper<Athlete> athleteQW = Wrappers.query();
        athleteQW.eq("UserID", uid);
        Athlete athlete = athleteDao.selectOne(athleteQW);
        PageResult list = new PageResult();
        EventListDto listDto = new EventListDto(name, event, date, offset,
                                                pageSize);
        if(athlete == null){
            // System.out.println("管理员");
            list = projectService.list(listDto);
        }else {
            list = projectService.listByAthlete(listDto);
        }
        return Result.success(list);
    }

    /**
     * 添加项目
     *
     * @param projectDTO 项目 DTO
     *
     * @return {@code Result<String>}
     */
    @PostMapping()
    public Result<String> addProject(@RequestBody ProjectDTO projectDTO){
        
        projectService.add(projectDTO);

        return Result.success("添加项目成功");
    }

    /**
     * 获取项目
     *
     * @param id 编号
     *
     * @return {@code Result<ProjectDTO>}
     */
    @GetMapping("/{id}")
    public Result<ProjectDTO> get(@PathVariable Long id){
        ProjectDTO projectDTO = projectService.getProject(id);
        
        return Result.success(projectDTO);
    }

    /**
     * 删除
     *
     * @param id 编号
     *
     * @return {@code Result<String>}
     *
     * @throws SQLException SQLException
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable long id) throws SQLException {
        
        projectService.delete(id);
        
        return Result.success("删除成功");
    }
    
    @PutMapping("/{id}")
    public Result<String> update(@RequestBody ProjectDTO projectDTO,
                         @PathVariable Long id){
        log.info("项目更新:{}", projectDTO);
        projectService.update(projectDTO,id);
        
        return Result.success("修改成功");
    }
    
    @PostMapping("/join")
    public Result<String> join(@RequestBody JoinProjectDTO joinProjectDTO) throws MyException {
        projectService.join(joinProjectDTO);
        return Result.success("成功报名，请等待审核");
    }
    
    
}
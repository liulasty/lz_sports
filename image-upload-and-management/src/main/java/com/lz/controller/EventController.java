package com.lz.controller;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/10/29/8:45
 * @Description:
 */

import com.lz.pojo.dto.EventDTO;
import com.lz.pojo.dto.EventListDto;
import com.lz.pojo.entity.Event;
import com.lz.pojo.result.MainPageData;
import com.lz.pojo.result.PageResult;
import com.lz.pojo.result.Result;
import com.lz.pojo.result.chart.OrderData;
import com.lz.pojo.result.chart.TypeData;
import com.lz.pojo.result.chart.UserData;
import com.lz.pojo.result.chart.UserType;
import com.lz.pojo.vo.EventVO;
import com.lz.service.EventService;
import com.lz.service.SportsImgService;
import com.lz.service.UserService;
import com.lz.utils.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author lz
 */
@RestController
@RequestMapping("sports/event")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class EventController {

    @Autowired
    private EventService eventService;
    
    @Autowired
    private SportsImgService sportsImgService;
    
    @Autowired
    private UserService userService;

    /**
     * 添加事件
     *
     * @param eventDTO 事件 DTO
     *
     * @return {@code String}
     *
     * @throws SQLIntegrityConstraintViolationException SQLColiction 约束冲突异常
     */
    @PostMapping("/EventList")
    public String addEvent(@RequestBody EventDTO eventDTO) throws SQLIntegrityConstraintViolationException {

        System.out.println("eventDTO.getImageUrls():" + Arrays.toString(eventDTO.getImageUrls()));
        return eventService.addEvent(eventDTO);


    }

    /**
     * 分页列表
     *
     * @param name        名字
     * @param type        类型
     * @param date        日期
     * @param currentPage 当前页面
     * @param pageSize    页面大小
     *
     * @return {@code Result<PageResult>}
     */
    @GetMapping("/page")
    public Result<PageResult> list(@RequestParam(required = false) String name,
                                   @RequestParam(required = false) String type,
                                   @RequestParam(required = false) String date,
                                   @RequestParam(defaultValue = "1") int currentPage,
                                   @RequestParam(defaultValue = "5") int pageSize) {

        EventListDto eventListDto = new EventListDto(name, type, date, currentPage, pageSize);

        try {
            
            System.out.println("eventListDto:" + eventListDto);
            PageResult list = eventService.list(eventListDto);

            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();

            return Result.error("未知错误");
        }
    }

    /**
     * 主页数据结果
     *
     * @return {@code Result<MainPageData>}
     */
    @GetMapping("/mainPageData")
    public Result<MainPageData> mainPageDataResult() {
        try {
            MainPageData mainPageData = new MainPageData();
            mainPageData.setTableData(eventService.getNewTen());
            OrderData<TypeData> orderData = new OrderData<>();

            orderData.getData().clear();
            List<TypeData> typeDataList = new ArrayList<>();
            UserData[] userData = new UserData[orderData.getDate().length];
            String[] months= new String[orderData.getDate().length];
            // 获取当前日期
            LocalDate currentDate = LocalDate.now();
            // 定义日期格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM"); 
            for (int i = 0; i < orderData.getDate().length; i++){
                months[i] = currentDate.minusMonths(i).format(formatter);
                typeDataList.add(eventService.getDataByDate(orderData.getDate()[i]));
                userData[i] =
                        userService.getUserNumsByMonth(months[i]);
                
            }
            List<UserType> userTypes =userService.getUserTypes();
            orderData.setData(typeDataList);
            
            mainPageData.setOrderData(orderData);
            mainPageData.setUserData(userData);
            mainPageData.setDoughnutData(userTypes);
            
            int[] nums = userService.getNums();
            
            mainPageData.setTotalNumberAthletes(nums[0]);
            mainPageData.setTotalNumberActivities(nums[1]);
            mainPageData.setTotalNumberProjects(nums[2]);
            mainPageData.setNewAthletesAddedMonth(nums[3]);
            mainPageData.setNewActivitiesAddedMonth(nums[4]);
            mainPageData.setNewProjectsAddedMonth(nums[5]);
            
            return Result.success(mainPageData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("未知错误");
        }


    }

    /**
     * 查找
     *
     * @param eventId 事件 ID
     *
     * @return {@code Result<EventVO>}
     */
    @GetMapping("/{eventId}")
    public Result<EventVO> select(@PathVariable Long eventId) throws NullPointerException{

        try {
            Event event = eventService.getEventId(eventId);
            List<String> eventImg = sportsImgService.selectImgs(event.getEventId(),
                                                                "活动图片");
            EventVO eventVO = EventVO.builder()
                    .name(event.getEventName())
                    .date(DataUtil.dateToString(event.getRegistrationStart()))
                    .fee(String.valueOf(event.getRegistrationFee()))
                    .type(event.getEligibility())
                    .end(DataUtil.dateToString(event.getRegistrationDeadline()))
                    .imageUrls(eventImg)
                    .build();

            return Result.success(eventVO);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.error("查询失败");
    }

    /**
     * 删除事件
     *
     * @param deleteEventId 删除事件 ID
     *
     * @return {@code Result}
     *
     * @throws SQLException SQLException
     */
    @DeleteMapping("/{deleteEventId}")
    public Result deleteEvent(@PathVariable String deleteEventId) throws SQLException {
        
        String flag = eventService.deleteEvent(deleteEventId);
        
        return Result.success(flag);
    }

    /**
     * 字符串到数据
     *
     * @param s s
     *
     * @return {@code Date}
     */
    public Date StringToData(String s) {

        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = formatter.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }


    /**
     * 修改
     * @param eventId
     * @param eventDTO
     *
     * @return {@code Result}
     */
    @PutMapping("/{eventId}")
    public Result update(@PathVariable String eventId,@RequestBody EventDTO eventDTO){
        eventService.update(eventId,eventDTO);
        
        return Result.success("修改成功");
    }


    /**
     * 事件类型
     *
     * @return {@code Result<String[]>}
     */
    @GetMapping("/eventType")
    public Result<List<Map<Long,String>>> eventType(){

        List<Map<Long,String>> types = eventService.getEventType();
        
        return Result.success(types);
    }
}
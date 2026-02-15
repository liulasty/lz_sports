package com.lz.service.impl;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/10/30/9:08
 * @Description:
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.Dao.EventDao;
import com.lz.Dao.SportsImgDao;

import com.lz.pojo.dto.EventDTO;
import com.lz.pojo.dto.EventListDto;
import com.lz.pojo.dto.ImgDTO;
import com.lz.pojo.entity.Event;
import com.lz.pojo.entity.SportsImg;
import com.lz.pojo.result.PageResult;
import com.lz.pojo.result.chart.TableData;
import com.lz.pojo.result.chart.TypeData;
import com.lz.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author lz
 */
@Service
@Transactional
public class EventServiceImpl implements EventService {

    @Autowired
    private EventDao eventDao;

    @Autowired
    private SportsImgDao sportsImgDao;
    
    /**
     * 字符串到日期
     *
     * @param s s
     *
     * @return {@code Date}
     */
    public Date stringToData(String s) {

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
     * 添加事件
     *
     * @param eventDTO 事件 DTO
     *
     * @return {@code String}
     *
     * @throws SQLIntegrityConstraintViolationException SQLColiction 约束冲突异常
     */
    @Override
    public String addEvent(EventDTO eventDTO) throws SQLIntegrityConstraintViolationException {

        LambdaQueryWrapper<Event> lqw = new LambdaQueryWrapper<Event>();
        lqw.eq(null != eventDTO.getName() && !"".equals(eventDTO.getName()),
               Event::getEventName,
               eventDTO.getName());
        if (eventDao.selectOne(lqw) != null) {
            throw new SQLIntegrityConstraintViolationException("名字重复");
        }

        try {
            System.out.println("eventDTO:" + eventDTO);


            Event event = Event.builder()
                    .eventName(eventDTO.getName())
                    .eligibility(eventDTO.getType())
                    .registrationFee(Integer.parseInt(eventDTO.getFee()))
                    .registrationStart(stringToData(eventDTO.getDate1()[0]))
                    .registrationDeadline(stringToData((eventDTO.getDate1()[1])))
                    
                    .build();


            eventDao.insertEvent(event);
            System.out.println("insert:" + event);
            if (eventDTO.getImageUrls().length > 0) {
                System.out.println("eventDTO.getImageUrls()[0].getOssUrl():" + eventDTO.getImageUrls()[0].getOssUrl());
            }
            for (ImgDTO imageUrl : eventDTO.getImageUrls()) {
                SportsImg sportsImg = SportsImg.builder().imgType("活动图片").imgSrc(imageUrl.getOssUrl()).typeId(event.getEventId()).build();
                sportsImgDao.insert(sportsImg);
            }
            return String.valueOf(event.getEventId());
        } catch (Exception e) {
            e.printStackTrace();

            return "异常消息，系统异常";
        }

    }

    /**
     * 分页列表
     *
     * @param eventListDto 事件列表 DTO
     *
     * @return {@code PageResult}
     */
    @Override
    public PageResult list(EventListDto eventListDto) {

        IPage<Event> page = new Page<>(
                eventListDto.getCurrentPage(),
                eventListDto.getPageSize());
        LambdaQueryWrapper<Event> lqw = new LambdaQueryWrapper<Event>();
        lqw.ge(null != eventListDto.getDate(), Event::getRegistrationStart,
               eventListDto.getDate());
        lqw.eq(null != eventListDto.getType() && !"".equals(eventListDto.getType()), Event::getEligibility,
               eventListDto.getType());
        lqw.like(null != eventListDto.getName() && !"".equals(eventListDto.getName()), Event::getEventName,
                 eventListDto.getName());


        // 根据 entity 条件，查询全部记录（并翻页）
        IPage<Event> eventIPage = eventDao.selectPage(page, lqw);

        System.out.println("eventIPage:" + eventIPage.getRecords());


        return new PageResult(eventIPage.getTotal(), eventIPage.getRecords());
    }

    /**
     * 获取新十
     *
     * @return {@code TableData[]}
     */
    @Override
    public TableData[] getNewTen() {
        IPage<Event> page = new Page<>(
                1, 10);
        LambdaQueryWrapper<Event> lqw = new LambdaQueryWrapper<Event>();
        lqw.orderBy(true, true, Event::getRegistrationStart);
        IPage<Event> eventIPage = eventDao.selectPage(page, lqw);
        int total = Math.toIntExact(eventIPage.getRecords().size());
        TableData[] tableData = new TableData[total];
        for (int i = 0; i < total; i++) {
            Event event = eventIPage.getRecords().get(i);

            // 创建一个新的 TableData 对象并将数据复制到其中
            TableData tableDatum = new TableData();
            tableDatum.setDate(event.getRegistrationStart());
            tableDatum.setName(event.getEventName());
            tableDatum.setFee(event.getRegistrationFee());
            tableDatum.setType(event.getEligibility());

            // 将 tableDatum 存储到 tableData 数组中
            tableData[i] = tableDatum;
        }

        return tableData;
    }

    /**
     * 获取事件 ID
     *
     * @param eventId 事件 ID
     *
     * @return {@code Event}
     */
    @Override
    public Event getEventId(Long eventId) {
        Event event = eventDao.selectById(eventId);
        return event;
    }

    /**
     * 删除事件
     *
     * @param eventId 事件 ID
     *
     * @return {@code String}
     *
     * @throws SQLException SQLException
     */
    @Override
    public String deleteEvent(String eventId) throws SQLException {
        

        int delete = eventDao.deleteById(eventId);
        System.out.println("delete:" + delete);
        if (delete == 0) {
            throw new SQLException("删除失败，请刷新重试");
        }

        sportsImgDao.deleteByTypeId(eventId);
        return "删除成功";
    }

    /**
     * 更新
     *
     * @param eventId  事件 ID
     * @param eventDTO 事件 DTO
     */
    @Override
    public void update(String eventId, EventDTO eventDTO) {
        System.out.println("eventDTO:" + eventDTO);
        Event event = Event.builder()
                .eventId(Long.valueOf(eventId))
                .eventName(eventDTO.getName())
                .eligibility(eventDTO.getType())
                .registrationFee(Integer.parseInt(eventDTO.getFee()))
                .registrationStart(stringToData(eventDTO.getDate1()[0]))
                .registrationDeadline(stringToData((eventDTO.getDate1()[1])))
                .build();

        eventDao.updateById(event);
        for (ImgDTO imageUrl : eventDTO.getImageUrls()) {
            if (!"old".equals(imageUrl.getType())) {
                SportsImg sportsImg =
                        SportsImg.builder().imgType("活动图片").imgSrc(imageUrl.getOssUrl()).typeId(event.getEventId()).build();
                sportsImgDao.insert(sportsImg);
            }

        }

        for (ImgDTO deleteImagesUrl : eventDTO.getDeleteImagesUrls()) {
            String deleteImage =
                    deleteImagesUrl.getOssUrl().substring(deleteImagesUrl.getOssUrl().lastIndexOf('/')+1);
            sportsImgDao.deleteByTypeIdAndSrc(event.getEventId(), deleteImage);
        }


    }

    /**
     * 获取事件类型
     *
     * @return {@code String[]}
     */
    @Override
    public List<Map<Long,String>> getEventType() {

        List<Map<Long,String>> strings = eventDao.selectEventName();

        return strings;
    }

    /**
     * 按日期获取数据
     *
     * @param date s
     *
     * @return {@code TypeData}
     */
    @Override
    public TypeData getDataByDate(String date) {

        TypeData typeData = new  TypeData();
        TypeData typeDataResult = eventDao.selectNumsByDate(date);
        
        if(typeDataResult != null){
            typeData = typeDataResult;
        }
        
        return typeData;
    }

}
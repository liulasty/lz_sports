package com.lz.pojo.dto;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/10/31/9:04
 * @Description:
 */

import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lz
 */
@Data
public class EventListDto {
    private String name;
    private String type;
    private Date date;
    private long currentPage;
    private long pageSize;

    public Date StringToData(String s){

        if(s == null || s.equals("")){
            return null;
        }
        Date date = null;
        try {
            String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            date = formatter.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        return date;
    }

    public EventListDto(String name, String type, String date, int currentPage,
                        int pageSize) {
        this.name = name;
        this.type = type;
        this.date = this.StringToData(date);
        this.currentPage= currentPage;
        this.pageSize = pageSize;
    }
    
    public EventListDto(){
        
    }
}
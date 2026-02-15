package com.lz.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Event List DTO (Pagination & Filter)
 */
public class EventListDTO {
    private String name;
    private String type;
    private Date date;
    private long currentPage;
    private long pageSize;

    public EventListDTO() {
    }

    public EventListDTO(String name, String type, String dateStr, int currentPage, int pageSize) {
        this.name = name;
        this.type = type;
        this.date = stringToDate(dateStr);
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    private Date stringToDate(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            return formatter.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(long currentPage) {
        this.currentPage = currentPage;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}

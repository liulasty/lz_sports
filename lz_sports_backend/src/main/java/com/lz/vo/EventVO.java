package com.lz.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Event View Object
 */
@Data
@Builder
public class EventVO implements Serializable {
    private Long id;
    private String date;
    private String end;
    private String name;
    private String fee;
    private String type;
    private List<String> imageUrls;
}

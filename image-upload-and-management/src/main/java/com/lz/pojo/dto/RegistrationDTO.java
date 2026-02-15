package com.lz.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 注册 DTO
 *
 * @author lz
 * @date 2023/12/06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationDTO implements Serializable {
    
    private Long id;
    private Long athleteId;
    private String athleteName;
    private Long eventId;
    private String eventName;
    private Long itemId;
    private String itemName;
    
    private Date registrationTime;
    private String registrationStatus;
}
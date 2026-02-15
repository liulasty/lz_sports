package com.lz.pojo.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author lz
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinProjectDTO implements Serializable {
    private Long eventId;
    private Long projectId;
    private Long userId;
}
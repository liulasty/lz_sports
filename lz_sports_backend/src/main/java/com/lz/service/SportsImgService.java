package com.lz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lz.entity.SportsImg;

import java.util.List;

/**
 * Sports Image Service Interface
 */
public interface SportsImgService extends IService<SportsImg> {
    
    List<String> selectImgs(Long id, String type);

    void addSrc(SportsImg sportsImg);

    String selectImg(Long userId, String avatar);
}

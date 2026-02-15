package com.lz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.entity.SportsImg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Sports Image Mapper
 */
@Mapper
public interface SportsImgMapper extends BaseMapper<SportsImg> {

    List<String> selectImgSrcByTypeAndId(@Param("imgType") String imgType, @Param("typeId") Long typeId);

    void deleteByTypeIdAndImgSrcAndImgType(@Param("typeId") Long typeId, @Param("src") String[] src, @Param("imgType") String imgType);

    void addImageSrcs(@Param("type") String type, @Param("imgId") Long imgId, @Param("imgSrc") String[] imgSrc);

    // Kept from original DAO as needed, but some might be replaced by MyBatis-Plus standard methods
    // e.g., deleteByImgTypeAndTypeId can be done via QueryWrapper in Service
}

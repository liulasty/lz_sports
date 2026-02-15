package com.lz.Dao;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.pojo.dto.ProjectDTO;
import com.lz.pojo.entity.SportsImg;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 体育 img dao
 *
 * @author lz
 * @date 2023/12/06
 */
@Mapper
public interface SportsImgDao extends BaseMapper<SportsImg> {
    /**
     * 按类型和ID选择IMG SRC
     *
     * @param imgType IMG型
     * @param typeId  类型 ID
     *
     * @return {@code List<String>}
     */
    List<String> selectImgSrcByTypeAndId(@Param("imgType") String imgType, @Param("typeId") Long typeId);


    /**
     * 按 img 类型和类型 id 删除
     *
     * @param imgType IMG型
     * @param typeId  类型 ID
     *
     * @return int
     */
    @Delete("DELETE FROM lz_sports.sportsimg WHERE ImgType = #{imgType} AND " +
            "typeid = #{typeId}")
    int deleteByImgTypeAndTypeId(String imgType,Long typeId);
    

    /**
     * 按 typeid 删除
     * 按 typeid 删除
     *
     * @param typeId 类型 ID
     *
     * @return int
     */
    @Delete("DELETE FROM lz_sports.sportsimg WHERE typeid = #{typeId}")
    int deleteByTypeId(@Param("typeId") String typeId);

    /**
     * 按类型 ID 和 SRC 删除
     *
     * @param typeId 类型 ID
     * @param ossUrl OSS URL
     */
    @Delete("DELETE FROM lz_sports.sportsimg WHERE typeid = #{typeId} && " +
            "ImgSrc = #{src}")
    void deleteByTypeIdAndSrc(@Param("typeId")Long typeId,
                              @Param("src") String ossUrl);

    /**
     * 按类型删除 ID 和 img SRC 和 img 类型
     *
     * @param typeId  类型 ID
     * @param src     来源
     * @param imgType IMG型
     */
    void deleteByTypeIdAndImgSrcAndImgType(Long typeId, String[] src, String imgType);


    /**
     * 添加图片 src
     * 添加 imageSrc
     *
     * @param type   类型
     * @param imgId  图片 ID
     * @param imgSrc 图片 src
     */
    void addImageSrcs(String type,Long imgId,String[] imgSrc);

    /**
     * 添加图片 src
     * 添加图片 src
     *
     * @param type   类型
     * @param imgSrc 图片 src
     * @param typeId 类型 ID
     */
    @Insert("insert into lz_sports.sportsimg(ImgType, typeId, ImgSrc) VALUES " +
            "(#{type}, #{typeId}, #{imgSrc})")
    void addImageSrc(String type,Long typeId,String imgSrc);

    /**
     * 按类型 ID 和 img src 批量删除
     *
     * @param typeId 类型 ID
     * @param src    来源
     */
    void deleteByTypeIdAndImgSrc(Long typeId, String[] src);
}
package com.lz.service;

import com.lz.pojo.entity.SportsImg;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author lz
 * @date 2024/04/03
 */
public interface SportsImgService {
    /**
     * 查找IMGs
     *
     * @param id   编号
     * @param type 类型
     *
     * @return {@code String[]}
     */
    List<String> selectImgs(Long id, String type);

    /**
     * 添加 图片src
     *
     * @param sportsImg 体育IMG
     *
     * @throws Exception 例外
     */
    void addSrc(SportsImg sportsImg) throws Exception;

    /**
     * 查找 img
     *
     * @param userId 用户 ID
     * @param avatar 化身
     *
     * @return {@code SportsImg}
     */
    String selectImg(Long userId, String avatar);

    List<SportsImg> selectByMap(HashMap<String, Object> map);
}
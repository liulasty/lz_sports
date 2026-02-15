package com.lz.service.impl;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/14/11:31
 * @Description:
 */

import com.lz.Exception.AnotherCustomException;
import com.lz.Exception.CustomException;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lz.Dao.SportsImgDao;
import com.lz.pojo.entity.SportsImg;
import com.lz.service.SportsImgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

/**
 * Sports IMG服务实现
 *
 * @author lz
 * @date 2023/11/14
 */
@Service
@Transactional(rollbackFor = {CustomException.class, AnotherCustomException.class})
public class SportsImgServiceImpl implements SportsImgService {

    @Autowired
    private SportsImgDao sportsImgDao;

    /**
     * 选择IMG
     *
     * @param id
     * @param type
     *
     * @return {@code String[]}
     */
    @Override
    public List<String> selectImgs(Long id, String type) {

        List<String> list = sportsImgDao.selectImgSrcByTypeAndId(type, id);

        return list;
    }

    /**
     * 添加 图片src
     *
     * @param sportsImg 体育IMG
     */
    @Override
    public void addSrc(SportsImg sportsImg) throws Exception {

        // 更新用户信息的业务逻辑
        if ("avatar".equals(sportsImg.getImgType())) {
            QueryWrapper<SportsImg> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("imgType", "avatar")
                    .eq("typeId", sportsImg.getTypeId());

            sportsImgDao.delete(queryWrapper);
        }

        if (sportsImg.getImgType() == null) {
            throw new CustomException("空");
        }
        
        if(sportsImg.getTypeId() == null){
            throw new AnotherCustomException("空");
        }
        sportsImgDao.insert(sportsImg);


    }

    /**
     * 查找 img
     *
     * @param userId 用户 ID
     * @param avatar 化身
     *
     * @return {@code SportsImg}
     */
    @Override
    public String selectImg(Long userId, String avatar) {
        QueryWrapper<SportsImg> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("imgType", avatar)
                .eq("typeId", userId);
        SportsImg sportsImg = sportsImgDao.selectOne(queryWrapper);
        if(sportsImg == null){
            sportsImg = new SportsImg();
            sportsImg.setImgSrc("https://image-upload-and-management.oss-cn-beijing.aliyuncs.com" +
                                       "/avatar/00d11ae2-133e-4025-aada-41274452aca8" +
                                       "-wallhaven-3zvypv.jpg");
        }
        return sportsImg.getImgSrc();
    }

    @Override
    public List<SportsImg> selectByMap(HashMap<String, Object> map) {
        List<SportsImg> sportsImgs = sportsImgDao.selectByMap(map);
        return sportsImgs;
    }
}
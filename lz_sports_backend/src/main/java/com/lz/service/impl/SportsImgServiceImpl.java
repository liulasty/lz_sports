package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.exception.BusinessException;
import com.lz.entity.SportsImg;
import com.lz.mapper.SportsImgMapper;
import com.lz.service.SportsImgService;
import com.lz.util.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sports Image Service Implementation
 */
@Service
@Slf4j
public class SportsImgServiceImpl extends ServiceImpl<SportsImgMapper, SportsImg> implements SportsImgService {

    @Autowired
    private SportsImgMapper sportsImgMapper;

    @Autowired
    private ImageUtils imageUtils;

    @Override
    public List<String> selectImgs(Long id, String type) {
        return sportsImgMapper.selectImgSrcByTypeAndId(type, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSrc(SportsImg sportsImg) {
        if (sportsImg.getImgType() == null || sportsImg.getTypeId() == null) {
            throw new BusinessException("参数不能为空");
        }

        // 如果是头像，先删除旧的
        if ("avatar".equals(sportsImg.getImgType())) {
            QueryWrapper<SportsImg> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ImgType", "avatar")
                    .eq("typeId", sportsImg.getTypeId());
            sportsImgMapper.delete(queryWrapper);
        }

        sportsImgMapper.insert(sportsImg);
        log.info("图片添加成功: Type={}, TypeId={}", sportsImg.getImgType(), sportsImg.getTypeId());
    }

    @Override
    public String selectImg(Long userId, String avatar) {
        LambdaQueryWrapper<SportsImg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SportsImg::getImgType, avatar)
                .eq(SportsImg::getTypeId, userId);
        SportsImg sportsImg = sportsImgMapper.selectOne(queryWrapper);
        
        if (sportsImg == null) {
            // 默认头像
            return imageUtils.getRandomFallbackUrl();
        }
        return sportsImg.getImgSrc();
    }
}

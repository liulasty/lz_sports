package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.common.context.BaseContext;
import com.lz.common.enums.NotificationType;
import com.lz.common.exception.BusinessException;
import com.lz.common.result.PageResult;
import com.lz.entity.User;
import com.lz.entity.Notification;
import com.lz.mapper.NotificationMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.NotificationService;
import com.lz.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(Long userId, String title, String content, NotificationType type) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .type(type)
                .isRead(false)
                .build();
        notification.initTime();
        notificationMapper.insert(notification);
        userMapper.incrementUnreadCount(userId);
    }

    @Override
    @Async
    public void batchSendNotification(List<Long> userIds, String title, String content, NotificationType type) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            sendNotification(userId, title, content, type);
        }
    }

    @Override
    public PageResult list(Integer currentPage, Integer pageSize, Boolean isRead) {
        Long userId = BaseContext.getCurrentId();
        Page<Notification> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        if (isRead != null) {
            queryWrapper.eq(Notification::getIsRead, isRead);
        }
        IPage<Notification> result = notificationMapper.selectPage(page, queryWrapper);
        List<NotificationVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult(result.getTotal(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long notificationId) {
        Long userId = BaseContext.getCurrentId();
        if (notificationMapper.countOwned(notificationId, userId) == 0) {
            throw new BusinessException("只能标记自己的通知", 403);
        }
        int updated = notificationMapper.markRead(notificationId, userId);
        if (updated > 0) {
            userMapper.decrementUnreadCount(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead() {
        Long userId = BaseContext.getCurrentId();
        notificationMapper.markAllRead(userId);
        userMapper.resetUnreadCount(userId);
    }

    @Override
    public Integer unreadCount() {
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            return 0;
        }
        return user.getUnreadCount() == null ? 0 : user.getUnreadCount();
    }

    private NotificationVO toVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setType(notification.getType());
        vo.setIsRead(notification.getIsRead());
        vo.setCreateTime(notification.getCreateTime());
        return vo;
    }
}

package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.context.BaseContext;
import com.lz.common.enums.RegistrationStatus;
import com.lz.common.enums.NotificationType;
import com.lz.common.enums.UserRole;
import com.lz.common.enums.UserStatus;
import com.lz.common.enums.AthleteStatus;
import com.lz.common.exception.BusinessException;
import com.lz.common.result.PageResult;
import com.lz.dto.RegistrationAndAthleteDTO;
import com.lz.dto.RegistrationDTO;
import com.lz.entity.Athlete;
import com.lz.entity.Event;
import com.lz.entity.Project;
import com.lz.entity.Registration;
import com.lz.entity.User;
import com.lz.mapper.AthleteMapper;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.UserMapper;
import com.lz.mapper.EventAdminMappingMapper;
import com.lz.service.EligibilityService;
import com.lz.service.NotificationService;
import com.lz.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.net.URLEncoder;

/**
 * 报名服务实现
 */
import com.lz.common.enums.EventStatus;
import com.alibaba.excel.EasyExcel;
import com.lz.util.StringUtils;
import com.lz.vo.RegistrationExportVO;

import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.beans.BeansException;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService, ApplicationContextAware {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final RegistrationMapper registrationMapper;
    private final AthleteMapper athleteMapper; // Note: athleteMapper now maps to sys_user, need verification
    private final EventMapper eventMapper;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final EventAdminMappingMapper eventAdminMappingMapper;
    private final NotificationService notificationService;
    private final RedissonClient redissonClient;
    private final EligibilityService eligibilityService;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long projectId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 防重放/幂等性控制：5秒内同一个用户对同一个项目只能发起一次报名请求
        String idempotencyKey = "registration:idempotency:" + userId + ":" + projectId;
        boolean isFirst = redissonClient.getBucket(idempotencyKey).trySet("1", 5, TimeUnit.SECONDS);
        if (!isFirst) {
            throw new BusinessException("您的请求过于频繁，请稍后再试");
        }

        Project projectSeed = projectMapper.selectById(projectId);
        if (projectSeed == null) {
            throw new BusinessException("项目不存在");
        }
        Event eventSeed = eventMapper.selectById(projectSeed.getEventId());
        if (eventSeed == null) {
            throw new BusinessException("赛事不存在");
        }

        String userEventLockKey = "registration:lock:user_event:" + userId + ":" + eventSeed.getId();
        RLock userEventLock = redissonClient.getLock(userEventLockKey);

        String lockKey = "registration:lock:project:" + projectId;
        RLock lock = redissonClient.getLock(lockKey);
        User syncUser = null;
        Athlete syncAthlete = null;

        try {
            if (userEventLock.tryLock(5, 10, TimeUnit.SECONDS)) {
                try {
                    if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                        throw new BusinessException("系统繁忙，请稍后再试");
                    }
                    User user = userMapper.selectById(userId);
                    if (user == null) {
                        throw new BusinessException("用户不存在");
                    }
                    if (user.getStatus() == UserStatus.DISABLED) {
                        throw new BusinessException("账号已被禁用");
                    }
                    if (user.getUserType() == UserRole.SUPER_ADMIN || user.getUserType() == UserRole.EVENT_ADMIN) {
                        throw new BusinessException("管理员角色不可申请", 403);
                    }
                    
                    Project project = projectSeed;
            
                    Event event = eventSeed;

                    Athlete athlete = athleteMapper.selectOne(new LambdaQueryWrapper<Athlete>()
                            .eq(Athlete::getUserId, userId)
                            .eq(Athlete::getEventId, event.getId()));
                    if (athlete == null) {
                        throw new BusinessException("请先完善本赛事的运动员信息");
                    }
                    if (athlete.getAthleteState() != AthleteStatus.APPROVED) {
                        throw new BusinessException("请先申请并通过本赛事的运动员资格审核");
                    }
                    // 数据库兜底：在事务内对用户+赛事维度加行锁，避免分布式锁失效时击穿 maxItemsPerAthlete
                    Long lockedAthleteId = registrationMapper.lockAthleteRowForEvent(userId, event.getId());
                    if (lockedAthleteId == null) {
                        throw new BusinessException("运动员记录不存在，请先完善本赛事信息");
                    }
            
                    if (event.getEventStatus() != EventStatus.OPEN) {
                        throw new BusinessException("当前赛事不可报名");
                    }
            
                    Date now = new Date();
                    assertWithinRegistrationWindow(event, now, true, "报名");

                    Registration existingRegistration = getOne(new LambdaQueryWrapper<Registration>()
                            .eq(Registration::getAthleteId, userId)
                            .eq(Registration::getItemId, projectId)
                            .last("LIMIT 1"));
                    if (existingRegistration != null
                            && existingRegistration.getRegistrationStatus() != RegistrationStatus.CANCELLED
                            && existingRegistration.getRegistrationStatus() != RegistrationStatus.REJECTED) {
                        throw new BusinessException("您已报名该项目，请勿重复报名");
                    }

                    int registeredCount = registrationMapper.countActiveByUserAndEvent(userId, event.getId());
                    int maxItemsPerAthlete = event.getMaxItemsPerAthlete() == null ? 1 : event.getMaxItemsPerAthlete();
                    if (registeredCount >= maxItemsPerAthlete) {
                        throw new BusinessException("已达到本赛事最多报名" + maxItemsPerAthlete + "个项目的限制");
                    }

                    if (project.getStartTime() != null && project.getEndTime() != null) {
                        String conflictItemName = registrationMapper.findConflictItemName(userId, event.getId(), project.getStartTime(), project.getEndTime());
                        if (StringUtils.isNotBlank(conflictItemName)) {
                            throw new BusinessException("与您已报名的[" + conflictItemName + "]时间冲突");
                        }
                    }
            
                    // 使用资格规则引擎校验（替代旧 gender_limit + limit_dept_ids 硬编码）
                    com.lz.eligibility.engine.EligibilityResult eligibilityResult =
                            eligibilityService.check(userId, event.getId(), projectId);
                    if (!eligibilityResult.isPassed()) {
                        throw new BusinessException(eligibilityResult.getReason());
                    }
            
                    int updated = projectMapper.incrementAttendance(projectId, project.getMaxAttendance());
                    if (updated == 0) {
                        throw new BusinessException("该项目报名人数已满");
                    }
                    if (existingRegistration != null) {
                        existingRegistration.setEventId(event.getId());
                        existingRegistration.setRegistrationTime(now);
                        existingRegistration.setRegistrationStatus(RegistrationStatus.PENDING);
                        existingRegistration.setRejectReason(null);
                        existingRegistration.setSchoolId(event.getSchoolId());
                        updateById(existingRegistration);
                    } else {
                        Registration registration = new Registration();
                        registration.setAthleteId(userId);
                        registration.setEventId(event.getId());
                        registration.setItemId(projectId);
                        registration.setRegistrationTime(now);
                        registration.setRegistrationStatus(RegistrationStatus.PENDING);
                        registration.setSchoolId(event.getSchoolId());
                        save(registration);
                    }
                    syncAthlete = athlete;
                    syncUser = user;
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                throw new BusinessException("系统繁忙，请稍后再试");
            }
            if (syncAthlete != null && syncUser != null) {
                // Get Spring proxy to ensure @Async and @Transactional work
                RegistrationService proxy = applicationContext.getBean(RegistrationService.class);
                proxy.syncAthleteProfileToUser(syncAthlete, syncUser);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统中断");
        } finally {
            if (userEventLock.isHeldByCurrentThread()) {
                userEventLock.unlock();
            }
        }
    }

    @Override
    public PageResult list(int currentPage, int pageSize, String name, String status, Date date) {
        Page<RegistrationDTO> page = new Page<>(currentPage, pageSize);
        IPage<RegistrationDTO> result = registrationMapper.selectRegistrationPage(page, name, status, date, null);
        return new PageResult(result.getTotal(), result.getRecords());
    }

    @Override
    public PageResult listByAthlete(int currentPage, int pageSize, String name, String status, Date date, Long athleteId) {
        Page<RegistrationDTO> page = new Page<>(currentPage, pageSize);
        IPage<RegistrationDTO> result = registrationMapper.selectRegistrationPage(page, name, status, date, athleteId);
        return new PageResult(result.getTotal(), result.getRecords());
    }

    @Override
    public java.util.Map<String, Object> getRegistrationStatsByEvent(Long eventId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        long total = count(new LambdaQueryWrapper<Registration>().eq(Registration::getEventId, eventId));
        long pending = count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, eventId)
                .eq(Registration::getRegistrationStatus, RegistrationStatus.PENDING));
        long approved = count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, eventId)
                .in(Registration::getRegistrationStatus, java.util.Arrays.asList(RegistrationStatus.APPROVED, RegistrationStatus.CONFIRMED)));
        long rejected = count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, eventId)
                .eq(Registration::getRegistrationStatus, RegistrationStatus.REJECTED));
                
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        
        return stats;
    }

    @Override
    public RegistrationAndAthleteDTO getDetail(Long id) {
        Registration r = getById(id);
        if (r == null) return null;

        RegistrationAndAthleteDTO dto = new RegistrationAndAthleteDTO();
        dto.setId(r.getId());
        dto.setApplyTime(r.getRegistrationTime());
        if (r.getRegistrationStatus() != null) {
            dto.setStatus(r.getRegistrationStatus().getStatus());
        }

        Athlete athlete = athleteMapper.selectById(r.getAthleteId());
        if (athlete != null) {
            dto.setName(athlete.getName());

            dto.setAge(Integer.parseInt(athlete.getAge()));
            dto.setGender(athlete.getGender());
            dto.setContact(athlete.getContact());
            com.lz.service.DepartmentService departmentService = applicationContext.getBean(com.lz.service.DepartmentService.class);
            dto.setAthleteGrade(departmentService.getFullDepartmentName(athlete.getDeptId()));
        }

        Event event = eventMapper.selectById(r.getEventId());
        if (event != null) {
            dto.setEventId(event.getId());
            dto.setEventName(event.getEventName());
        }

        Project project = projectMapper.selectById(r.getItemId());
        if (project != null) {
            dto.setItemId(project.getId());
            dto.setItemName(project.getItemName());
            dto.setNum(project.getAttendance());
            dto.setMaxNum(project.getMaxAttendance());
            if (project.getLimitation() != null) {
                dto.setLimitation(project.getLimitation().getLimit());
            }
            dto.setDeptName(project.getLimitDeptIds()); // Just returning JSON string for now
        }

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        Registration r = getById(id);
        if (r == null) throw new BusinessException("报名记录不存在");
        assertCanManageEvent(r.getEventId());
        if (r.getRegistrationStatus() != RegistrationStatus.PENDING) {
            throw new BusinessException("已审核的申请不可重复审核");
        }
        r.setRegistrationStatus(RegistrationStatus.APPROVED);
        updateById(r);
        notificationService.create(r.getAthleteId(), "报名审核通过", "您的报名已审核通过", NotificationType.SYSTEM);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuse(Long id, String reason) {
        Registration r = getById(id);
        if (r == null) throw new BusinessException("报名记录不存在");
        assertCanManageEvent(r.getEventId());
        if (r.getRegistrationStatus() != RegistrationStatus.PENDING) {
            throw new BusinessException("已审核的申请不可重复审核");
        }
        r.setRegistrationStatus(RegistrationStatus.REJECTED);
        r.setRejectReason(StringUtils.defaultIfBlank(reason, "无"));
        updateById(r);
        String notifyReason = StringUtils.defaultIfBlank(r.getRejectReason(), "无");
        notificationService.create(r.getAthleteId(), "报名审核拒绝", "您的报名已被拒绝，原因：" + notifyReason, NotificationType.SYSTEM);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        cancel(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        Registration r = getById(id);
        if (r == null) {
            throw new BusinessException("报名记录不存在");
        }
        Long currentUserId = BaseContext.getCurrentId();
        if (!r.getAthleteId().equals(currentUserId)) {
            throw new BusinessException("只能取消自己的报名", 403);
        }
        Event event = eventMapper.selectById(r.getEventId());
        if (event != null) {
            assertWithinRegistrationWindow(event, new Date(), false, "取消报名");
        }
        if (r.getRegistrationStatus() == RegistrationStatus.CANCELLED) {
            return;
        }
        r.setRegistrationStatus(RegistrationStatus.CANCELLED);
        updateById(r);
        int updated = projectMapper.decrementAttendance(r.getItemId());
        if (updated == 0) {
            throw new BusinessException("取消失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String batchAudit(List<Long> ids, boolean approve) {
        if (ids == null || ids.isEmpty()) {
            return "未提供审核ID";
        }
        int success = 0;
        int skipped = 0;
        List<Registration> pendingList = new ArrayList<>();
        java.util.Set<Long> pendingEventIds = new java.util.HashSet<>();
        for (Long id : ids) {
            Registration registration = getById(id);
            if (registration == null || registration.getRegistrationStatus() != RegistrationStatus.PENDING) {
                skipped++;
                continue;
            }
            pendingList.add(registration);
            if (registration.getEventId() != null) {
                pendingEventIds.add(registration.getEventId());
            }
        }

        if (pendingEventIds.size() > 1) {
            throw new BusinessException("批量审核仅支持同一赛事", 400);
        }
        if (!pendingEventIds.isEmpty()) {
            assertCanManageEvent(pendingEventIds.iterator().next());
        }

        for (Registration registration : pendingList) {
            if (approve) {
                registration.setRegistrationStatus(RegistrationStatus.APPROVED);
                notificationService.create(registration.getAthleteId(), "报名审核通过", "您的报名已审核通过", NotificationType.SYSTEM);
            } else {
                registration.setRegistrationStatus(RegistrationStatus.REJECTED);
                String reason = registration.getRejectReason() == null || registration.getRejectReason().isBlank() ? "无" : registration.getRejectReason();
                notificationService.create(registration.getAthleteId(), "报名审核拒绝", "您的报名已被拒绝，原因：" + reason, NotificationType.SYSTEM);
            }
            updateById(registration);
            success++;
        }
        return "已处理" + success + "条，跳过" + skipped + "条";
    }

    private void assertCanManageEvent(Long eventId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException("用户未登录", 401);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在", 401);
        }
        if (user.getUserType() == UserRole.SCHOOL_ADMIN || user.getUserType() == UserRole.SUPER_ADMIN) {
            return;
        }
        if (user.getUserType() != UserRole.EVENT_ADMIN) {
            throw new BusinessException("权限不足", 403);
        }
        long count = eventAdminMappingMapper.selectCount(
                new LambdaQueryWrapper<com.lz.entity.EventAdminMapping>()
                        .eq(com.lz.entity.EventAdminMapping::getEventId, eventId)
                        .eq(com.lz.entity.EventAdminMapping::getUserId, userId)
        );
        if (count == 0) {
            throw new BusinessException("您不是该赛事的管理员", 403);
        }
    }

    private void assertWithinRegistrationWindow(Event event, Date now, boolean requireOpenStatus, String action) {
        if (event == null) {
            throw new BusinessException("赛事不存在");
        }
        if (requireOpenStatus && event.getEventStatus() != EventStatus.OPEN) {
            throw new BusinessException("当前赛事不可" + action);
        }
        if (!requireOpenStatus && event.getEventStatus() == EventStatus.CLOSED) {
            throw new BusinessException("赛事已关闭，不可" + action);
        }
        if (event.getRegistrationStartTime() != null && now.before(event.getRegistrationStartTime())) {
            throw new BusinessException("不在可" + action + "时间范围内");
        }
        if (event.getRegistrationEndTime() != null && now.after(event.getRegistrationEndTime())) {
            throw new BusinessException("不在可" + action + "时间范围内");
        }
    }


    @Override
    public int getCountByAthlete(Long athleteId) {
        LambdaQueryWrapper<Registration> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Registration::getAthleteId, athleteId);
        return (int) count(lqw);
    }

    @Override
    public void export(Long eventId, jakarta.servlet.http.HttpServletResponse response) {
        // Fetch data
        List<RegistrationDTO> list = registrationMapper.selectRegistrationList(eventId);
        
        List<RegistrationExportVO> exportList = new ArrayList<>();
        for (RegistrationDTO dto : list) {
            RegistrationExportVO vo = new RegistrationExportVO();
            vo.setRegistrationId(dto.getRegistrationId());
            vo.setEventName(dto.getEventName());
            vo.setItemName(dto.getItemName());
            vo.setAthleteName(dto.getAthleteName());
            vo.setGender(dto.getGender());
            vo.setDeptName(dto.getDeptName());
            vo.setContact(dto.getContact());
            vo.setRegistrationTime(dto.getRegistrationTime());
            vo.setStatus(dto.getRegistrationStatus());
            exportList.add(vo);
        }

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("报名名单", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), RegistrationExportVO.class).sheet("名单").doWrite(exportList);
        } catch (Exception e) {
            log.error("Export failed", e);
            throw new BusinessException("导出失败");
        }
    }

    @Override
    public List<RegistrationDTO> listScoreEntryCandidates(Long eventId, Long itemId) {
        if (eventId == null) {
            throw new BusinessException("赛事ID不能为空", 400);
        }
        return registrationMapper.selectScoreEntryCandidates(eventId, itemId);
    }

    @Override
    @org.springframework.scheduling.annotation.Async
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void syncAthleteProfileToUser(Athlete athlete, User user) {
        boolean changed = false;
        if (StringUtils.isNotBlank(athlete.getName()) && !athlete.getName().equals(user.getName())) {
            user.setName(athlete.getName());
            changed = true;
        }
        if (StringUtils.isNotBlank(athlete.getContact()) && !athlete.getContact().equals(user.getStudentId())) {
            user.setStudentId(athlete.getContact());
            changed = true;
        }
        if (changed) {
            userMapper.updateById(user);
        }
    }
}

# LZ Sports 后端实现细节分析

## 1. 核心架构与工程规范
LZ Sports 后端基于 Spring Boot 3.2.3 构建，采用经典的 Controller-Service-Mapper 三层架构。
- **全局异常处理**：通过 `@RestControllerAdvice` 配合 `GlobalExceptionHandler`，统一将 `BusinessException` 转化为标准的 JSON `Result` 对象返回给前端。
- **统一上下文**：通过自定义 `BaseContext`（基于 `ThreadLocal`）在整个请求链路中透传当前登录用户的 ID，避免了频繁在方法间传递用户信息的麻烦。

## 2. 权限与安全体系 (Spring Security + JWT)

### 2.1 过滤器与配置机制
- **精简的 SecurityConfig**：抛弃了传统的繁琐配置，主要负责关闭 CSRF、设置无状态会话（STATELESS），并注册自定义的 401（`CustomAuthenticationEntryPoint`）和 403（`CustomAccessDeniedHandler`）处理器。
- **核心逻辑下沉**：所有的请求放行、Token 校验逻辑被统一收敛到 `JwtAuthenticationFilter` 中。
- **双重校验防串改**：在校验 JWT 签名有效性的同时，还会通过 Redis 核对当前用户的最新 Token（`auth:token:{userId}`）。这种设计有效支持了**单点登录互踢**以及**主动注销**功能。

### 2.2 角色与鉴权
- 用户登录成功后，系统会根据其类型（如 ATHLETE, EVENT_ADMIN, SCHOOL_ADMIN）将其封装为 `SimpleGrantedAuthority` 并加上 `ROLE_` 前缀，存入 `SecurityContextHolder`。
- Controller 接口使用 `@PreAuthorize("hasRole('SCHOOL_ADMIN')")` 等注解进行细粒度的接口级权限管控。

## 3. 核心业务模块分析

### 3.1 报名防超卖与高并发处理
报名接口（`RegistrationServiceImpl.add`）是整个系统中并发量最大的节点之一，系统在这里采取了多重防护机制：
1. **幂等性控制**：利用 Redisson 的 `trySet(key, 1, 5s)` 为每个“用户+项目”生成一个5秒有效期的标识，有效拦截用户的连续点击和脚本恶意重放。
2. **分布式锁**：使用 Redisson 的分布式锁 `RLock lock = redissonClient.getLock("registration:lock:project:" + projectId)` 控制并发写入，确保在校验报名资格（如性别、部门限制、时间冲突等）及扣减/增加项目名额时的原子性。
3. **乐观锁/数据库兜底**：在 `projectMapper.incrementAttendance` 更新已报名人数时，SQL 层面也会有相应的库存阈值校验兜底，防止超卖。

### 3.2 赛事状态流转 (定时任务)
系统中赛事的生命周期包括：DRAFT -> PUBLISHED -> OPEN(报名中) -> CLOSED(报名截止) -> ONGOING(比赛中) -> FINISHED(已结束)。
除了管理员手动切换外，系统内置了 `EventStatusScheduler` 定时任务：
- 通过 `@Scheduled(cron = "0 * * * * ?")` 每分钟执行一次。
- 自动扫描所有赛事，比对当前时间与 `reg_start_time`、`reg_deadline`、`start_time` 等时间节点，自动触发状态流转。

### 3.3 数据导入与导出
对于成绩的批量录入和报名人员的批量下载，系统深度集成了 **Alibaba EasyExcel**。
- **导出**：在 `export` 接口中，直接将组装好的 `RegistrationExportVO` 列表写入 HttpServletResponse 的输出流。
- **导入**：系统设计了 `ScoreImportListener` 来处理成绩 Excel 的流式读取，有效避免了 OOM 问题，并将校验失败的行记录到 `ScoreImportFailureVO` 统一返回给前端。

### 3.4 异步与事务传播
在报名成功后，系统会触发同步用户资料的方法 `syncAthleteProfileToUser`。该方法使用了 `@Async` 以及 `@Transactional(propagation = Propagation.REQUIRES_NEW)`。
这种设计确保了资料同步逻辑在一个**独立的新事务**中异步执行，不仅加快了主报名接口的响应速度，也保证了即使资料同步失败，主报名逻辑依然能够成功提交。

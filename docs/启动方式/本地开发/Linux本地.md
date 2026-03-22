# Linux 本地启动说明

适用场景：开发机为 Linux，前后端都直接在本机运行。

## 前置条件

- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL、Redis 已启动

## 启动步骤

1. 后端启动

```bash
cd /path/to/lz_sports/lz_sports_backend
mvn spring-boot:run
```

2. 前端启动（设置统一端口）

```bash
cd /path/to/lz_sports/lz_sports_frontend
export FRONTEND_PORT=5173
npm install
npm run dev
```

## 访问地址

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`

## 默认账号
- 账号：`admin`
- 密码：`Admin@123`
⚠️ 首次登录系统会强制要求修改密码，请及时更新。

## 改端口方式

只改这一行即可：

```bash
export FRONTEND_PORT=5200
```

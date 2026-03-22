# Windows 本地启动说明

适用场景：开发机为 Windows，前后端都直接在本机运行。

## 前置条件

- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL、Redis 已启动

## 启动步骤

1. 后端启动

```powershell
cd d:\soft\lz_sports\lz_sports_backend
mvn spring-boot:run
```

2. 前端启动（设置统一端口）

```powershell
cd d:\soft\lz_sports\lz_sports_frontend
$env:FRONTEND_PORT=5173
npm install
npm run dev
```

## 访问地址

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`

## 改端口方式

只改这一行即可：

```powershell
$env:FRONTEND_PORT=5200
```

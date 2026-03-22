# lz_sports 部署指南

## 环境要求
- JDK 21+
- MySQL 8.x
- Redis 7.x
- Node 18+（仅构建前端时需要）
- Nginx 1.20+

## 部署步骤

### 1. 数据库初始化
- 导入顺序：先执行 `init.sql` 创建数据库结构，再执行 `data.sql` 导入初始数据。
- 数据库字符集要求：**utf8mb4**

### 2. 后端配置
请在 `application-prod.yml` 中配置以下环境变量：

- **DB_URL**: 数据库连接地址，例如 `jdbc:mysql://localhost:3306/lz_sports?useUnicode=true&characterEncoding=utf-8&useSSL=false`
- **DB_USERNAME**: 数据库用户名，例如 `root`
- **DB_PASSWORD**: 数据库密码，例如 `password123`
- **REDIS_HOST**: Redis 服务地址，例如 `localhost`
- **REDIS_PORT**: Redis 服务端口，例如 `6379`
- **REDIS_PASSWORD**: Redis 连接密码（若无则留空）
- **MAIL_HOST**: 邮件服务 SMTP 地址，例如 `smtp.qq.com`
- **MAIL_USERNAME**: 邮箱发送账号，例如 `your_email@qq.com`
- **MAIL_PASSWORD**: 邮箱授权码，例如 `xxxxxxx`
- **JWT_SECRET**: JWT 签名密钥（需确保复杂度），例如 `your-256-bit-secret`
- **FILE_UPLOAD_PATH**: 文件上传绝对路径目录，例如 `/www/wwwroot/lz_sports/uploads/`

### 3. 启动后端
使用以下命令启动后端服务：
```bash
java -jar lz_sports-backend.jar --spring.profiles.active=prod
```

### 4. 前端构建与部署
在前端项目目录下执行以下命令进行构建：
```bash
npm install
npm run build
```
构建完成后，将 `dist/` 目录内容复制到 Nginx 的静态文件目录下（如 `/usr/share/nginx/html`）。

### 5. Nginx 配置
在 `nginx.conf` 中进行关键配置：
- **前端静态文件目录**：将 `root` 指向前端 `dist/` 部署的绝对路径。
- **API 反向代理**：将 `/api/` 路径代理到后端端口（例如 `http://localhost:8080/`）。
- **history 路由模式**：添加 `try_files $uri $uri/ /index.html;` 解决刷新页面报 404 的问题。

## 系统初始化

首次部署完成后，访问系统会自动跳转至初始化向导页面。

也可以直接调用初始化接口完成配置：

POST `/api/system/init`

请求体示例：
```json
{
  "schoolName": "XX大学",
  "logoUrl": "",
  "themeColor": "BLUE",
  "contactEmail": "admin@school.edu.cn",
  "adminUsername": "admin",
  "adminPassword": "Admin@123",
  "adminEmail": "admin@school.edu.cn",
  "grades": ["大一", "大二", "大三", "大四", "研究生"]
}
```

字段说明：
- **schoolName**：学校名称（必填）
- **logoUrl**：Logo 图片地址（选填，可初始化后在系统配置页上传）
- **themeColor**：主题色代码，可选值：BLUE / GREEN / RED / PURPLE / ORANGE / GRAY
- **contactEmail**：学校联系邮箱
- **adminUsername**：超级管理员登录用户名（必填）
- **adminPassword**：超级管理员初始密码（必填，8-20位含字母和数字）
- **adminEmail**：超级管理员邮箱（必填）
- **grades**：年级列表，初始化后可在系统配置中修改

注意：初始化接口只能调用一次，系统已初始化后再次调用将返回 409 错误。

### 6. 首次登录说明
系统部署完成后的初始账号：
- 默认超级管理员账号：`admin`
- 默认密码：`Admin@123`
- ⚠️ **注意**：首次登录系统会强制要求修改密码，请及时修改。

### 7. 常见问题
- **跨域问题如何排查**：检查 Nginx 配置中是否正确代理了 `/api` 请求，确保后端和前端运行在同一域名下，或者后端已正确配置 CORS 允许前端域名访问。
- **文件上传失败的权限问题**：请检查 `FILE_UPLOAD_PATH` 指定的目录是否存在，且运行后端的系统用户对该目录具有读写权限（Linux 系统可使用 `chmod 755` 或 `chown` 调整）。
- **Redis 连接失败的排查步骤**：确认 Redis 服务是否已启动；检查 `REDIS_HOST`、`REDIS_PORT` 和 `REDIS_PASSWORD` 是否与 Redis 实际配置匹配；检查防火墙是否放行了相应端口。

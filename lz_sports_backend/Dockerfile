# ======================== 构建阶段 (Build Stage) ========================
# 基于官方 Maven 镜像（内置 JDK 21）作为构建环境，命名为 build 阶段
# 选择 eclipse-temurin 是因为它是开源且合规的 OpenJDK 发行版，适合生产环境
FROM maven:3.9.6-eclipse-temurin-21 AS build

# 设置工作目录，后续所有操作都基于此目录，避免文件路径混乱
WORKDIR /app

# 先复制 pom.xml 文件：利用 Docker 分层缓存特性，优先缓存依赖包
# 好处：仅当 pom.xml 变更时，才会重新下载依赖，否则复用缓存层，加速构建
COPY pom.xml .

# 复制项目源代码到工作目录
COPY src ./src

# 执行 Maven 打包命令：
# - clean: 清理之前的构建产物
# - package: 编译并打包为 Jar 包
# - DskipTests: 跳过单元测试（构建阶段聚焦打包，测试可在CI/CD流程中单独执行）
RUN mvn clean package -DskipTests

# ======================== 运行阶段 (Run Stage) ========================
# 基于轻量级的 Alpine 版 JRE 21 镜像（仅包含运行时环境，减小镜像体积）
# Alpine 镜像体积小，安全性高，适合生产环境部署
FROM eclipse-temurin:21-jre-alpine

# 设置运行时的工作目录
WORKDIR /app

# 从构建阶段（build）复制打包好的 Jar 包到当前镜像，并命名为 app.jar（简化后续命令）
# lz_sports_backend-0.0.1-SNAPSHOT.jar 是项目实际打包后的 Jar 包名称，需与 pom.xml 中配置一致
COPY --from=build /app/target/lz_sports_backend-0.0.1-SNAPSHOT.jar app.jar

# 声明容器暴露的端口（仅为文档说明，实际端口映射需在 run 时通过 -p 指定）
# 8080 是 Spring Boot 应用默认的运行端口，需与项目配置文件中的 server.port 一致
EXPOSE 8080

# 容器启动时执行的命令：以 Jar 包方式运行 Spring Boot 应用
# ENTRYPOINT 结合 ["exec", "形式"] 是 Docker 最佳实践，确保信号能正确传递给 Java 进程
ENTRYPOINT ["java", "-jar", "app.jar"]
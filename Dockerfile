# ============================
# 第一阶段：编译构建 (Builder)
# ============================
# 使用官方 Maven 镜像，自动包含 Java 和 Maven 环境
FROM maven:3.8.5-openjdk-17 AS builder

# 设置工作目录
WORKDIR /app

# 1. 先只复制 pom.xml，下载依赖 (利用 Docker 缓存，加速后续构建)
COPY pom.xml .
# 下载依赖 (如果这次没改 pom.xml，这步会直接走缓存)
RUN mvn dependency:go-offline -B

# 2. 复制源码并打包
COPY src ./src
# 执行打包命令 (跳过测试，加快速度)
RUN mvn clean package -DskipTests


# ============================
# 第二阶段：运行环境 (Runner)
# ============================
# 使用精简版 JDK 运行
FROM openjdk:17-jdk-slim

# 设置维护者信息
LABEL maintainer="zhengshoudong"

# 设置工作目录
WORKDIR /app

# 关键步骤：从第一阶段 (builder) 拷贝编译好的 jar 包
# 注意：这里会自动找到 target 目录下生成的 jar
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
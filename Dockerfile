# 基础镜像
FROM openjdk:17-jdk-slim

# 作者
LABEL maintainer="zhengshoudong"

# 挂载目录
VOLUME /tmp

# 将 jar 包添加到容器中并更名为 app.jar
COPY target/*.jar app.jar

# 运行 jar 包
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
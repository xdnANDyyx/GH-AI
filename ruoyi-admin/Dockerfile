# 方案A：基于官方镜像进行扩展（推荐）
FROM openjdk:17-jdk-slim

# 设置时区等额外环境变量（可选）
ENV TZ=Asia/Shanghai \
    APP_HOME=/opt/app

WORKDIR /app
COPY ruoyi-admin.jar app.jar
# 如果存在配置文件目录，也可以复制进去，但一般通过外部挂载或打包在 jar 内
# 这里我们通过命令行参数指定 prod 配置
EXPOSE 8780
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=pre"]
# 构建阶段
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# 复制 Maven 配置
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# 下载依赖（利用缓存）
RUN ./mvnw dependency:go-offline -B

# 复制源码并构建
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建非 root 用户
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 复制构建产物
COPY --from=builder /app/target/kindergarten-system-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]

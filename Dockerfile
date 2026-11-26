# Stage 1: Build ứng dụng Spring Boot với Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copy pom.xml và tải dependencies trước để tận dụng Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn và build package (bỏ qua test khi đóng gói image)
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime container gọn nhẹ với JRE 17
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Tạo thư mục uploads
RUN mkdir -p /app/uploads

# Copy file jar từ builder
COPY --from=builder /build/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

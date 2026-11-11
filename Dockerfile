FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY app_java.java ./
COPY frontend/Page.html ./frontend/Page.html

ENV HOST=0.0.0.0
EXPOSE 5000

# Java 11+ can run a single source file directly, no separate compile step
CMD ["java", "app_java.java"]

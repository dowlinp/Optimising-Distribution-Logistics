FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY app_java.java home.html ./
COPY delivery/ ./delivery/
COPY frontend/ ./frontend/
COPY warehouse/ ./warehouse/
ENV HOST=0.0.0.0
EXPOSE 5000

# Java 11+ can run a single source file directly, no separate compile step
CMD ["java", "app_java.java"]

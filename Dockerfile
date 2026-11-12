
FROM eclipse-temurin:21-jdk

WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 python3-venv && \
    rm -rf /var/lib/apt/lists/*

RUN python3 -m venv /opt/venv && \
    /opt/venv/bin/pip install --no-cache-dir requests flexpolyline

ENV PATH="/opt/venv/bin:${PATH}"

COPY app_java.java home.html ./
COPY delivery/ ./delivery/
COPY frontend/ ./frontend/
COPY warehouse/ ./warehouse/
COPY videos/ ./videos/

ENV HOST=0.0.0.0

EXPOSE 5000

CMD ["java", "app_java.java"]

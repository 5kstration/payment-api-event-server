# =========================================================================
# Stage 1: Build Stage
# =========================================================================
FROM gradle:8.12-jdk17-alpine AS builder

WORKDIR /build

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon || true

COPY src src
RUN gradle bootJar -x test --no-daemon

# =========================================================================
# Stage 2: Runtime Stage
# =========================================================================
FROM eclipse-temurin:17-jre-alpine AS runner

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /build/build/libs/*.jar app.jar

RUN mkdir -p /app/config \
    && chown -R appuser:appgroup /app

USER appuser

ENV SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:/app/config/
ENV SIMULATION_DATA_FILE=file:/app/config/payment-simulation-data.json

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -q -O - http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Dsun.net.inetaddr.ttl=60", \
            "-jar", \
            "app.jar"]

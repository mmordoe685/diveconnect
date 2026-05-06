# ──────────────────────────────────────────────────────────────
# DiveConnect — Dockerfile multi-stage
#
# Stage 1: build con Maven (compila el JAR sin necesitar Maven local)
# Stage 2: runtime mínimo con JRE 17, ejecuta el JAR
#
# Ventajas:
#   - Imagen final ~250 MB (sin Maven ni código fuente)
#   - Build reproducible: misma versión de Maven y Java en local y CI
#   - Capa de dependencias cacheada para builds incrementales rápidos
# ──────────────────────────────────────────────────────────────

# ─── Stage 1: build ──────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 1. Copiar sólo pom.xml primero para cachear dependencias
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw.cmd ./
RUN mvn -B -q dependency:go-offline

# 2. Copiar el código y empaquetar
COPY src ./src
RUN mvn -B -q -DskipTests package

# ─── Stage 2: runtime ────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S diveconnect && adduser -S -G diveconnect diveconnect

# Directorio de uploads (volumen recomendado en producción)
RUN mkdir -p /app/uploads && chown diveconnect:diveconnect /app/uploads
VOLUME ["/app/uploads"]

# Copiar el JAR construido en el stage 1
COPY --from=build /app/target/diveconnect-*.jar /app/app.jar
RUN chown diveconnect:diveconnect /app/app.jar

USER diveconnect
EXPOSE 8080

# Healthcheck contra el endpoint público de configuración
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/api/paypal/config || exit 1

# Permitir override del Xmx vía env (Render limita la RAM gratuita a 512 MB)
ENV JAVA_OPTS="-Xms256m -Xmx450m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

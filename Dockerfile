# Dockerfile - Jib이 빌드한 이미지 기반 런타임 참조용 (직접 빌드 시 사용)
# 일반적으로 Jib으로 이미지를 빌드하므로 이 파일을 직접 사용하지 않습니다.
# 필요 시: docker build -t idp-auth-service .
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Gradle bootJar 산출물 복사
COPY build/libs/idp-auth-service-*.jar app.jar

ENV TZ=Asia/Seoul
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

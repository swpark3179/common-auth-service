# IDP Auth Service

IDP(Identity Provider) 인증 서비스는 Spring Boot 3.3.5 기반으로 개발된 마이크로서비스로, 시스템 내 사용자의 인증 및 인가 기능을 제공합니다. 특히 **Apache APISIX** API Gateway와의 연동을 지원하여, `ext-plugin-pre-req` 플러그인을 통한 중앙 집중식 권한 제어(RBAC)를 수행할 수 있도록 설계되었습니다.

## 주요 기능 (Core Features)

*   **인증 및 인가 (Authentication & Authorization):** JWT(JSON Web Token) 기반의 인증 처리를 수행합니다.
*   **APISIX 연동 (APISIX Integration):** APISIX API Gateway의 외부 인증(External Auth) 플러그인 연동 전용 엔드포인트(`/apisix/auth`)를 제공하여 Gateway 단에서 트래픽을 제어합니다.
*   **역할 기반 접근 제어 (RBAC - Role-Based Access Control):** 사용자의 역할(Role)과 권한(Permission)에 따른 세밀한 API 접근 제어를 지원합니다. (Ant Path Matcher 활용)
*   **다중 환경 지원 (Multi-Environment Support):**
    *   `dev`: 로컬 개발용 SQLite 환경 (인메모리/파일 DB) 및 Swagger UI 제공
    *   `prod`: 운영 환경용 Oracle Database 연동 및 Flyway를 통한 안정적인 스키마 마이그레이션 적용
*   **감사 로그 (Audit Logging):** 비동기 스레드풀을 활용하여 APISIX 인증 허용/거부 이력 등을 기록합니다.

## 기술 스택 (Tech Stack)

*   **Language:** Java 21 (Virtual Threads 활성화)
*   **Framework:** Spring Boot 3.3.5, Spring Security, Spring Data JPA
*   **Database:** Oracle DB 23c (Prod), SQLite (Dev/Test)
*   **Migration Tool:** Flyway
*   **Security / Auth:** JJWT 0.12.x
*   **Build Tool:** Gradle
*   **Containerization:** Jib (Docker/OCI 이미지 빌드), Docker Compose
*   **API Docs:** SpringDoc OpenAPI (Swagger UI)

## 개발 환경 설정 (Getting Started)

### 요구 사항
*   Java 21 JDK
*   Docker 및 Docker Compose (로컬 실행 및 Oracle 연동 테스트 시 필요)

### 로컬 실행 (개발 모드)

개발 모드(`dev` 프로파일)에서는 경량 DB인 SQLite를 사용하여 외부 DB 의존성 없이 빠르게 실행할 수 있습니다.

```bash
# 기본 환경 변수 파일 생성
cp .env.example .env

# 애플리케이션 빌드 및 실행 (dev 프로파일)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 운영 환경 빌드 및 실행 (Docker Compose)

`docker-compose.yml`을 사용하여 Oracle DB를 연동하고 운영 프로파일(`prod`)로 서비스를 실행할 수 있습니다.

```bash
# 1. Jib을 이용한 Docker 이미지 빌드
./gradlew jibDockerBuild

# 2. Docker Compose 실행
docker-compose up -d
```

## 환경 변수 설정 (Configuration)

`.env` 파일 또는 시스템 환경 변수를 통해 아래의 주요 설정값을 주입할 수 있습니다.

| 환경 변수명 | 설명 | 기본값 / 권장값 | 환경 |
| :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | 실행 프로파일 설정 | `dev` 또는 `prod` | 공통 |
| `JWT_SECRET` | JWT 서명용 시크릿 키 | **반드시 32자 이상 지정 필수** | 공통 |
| `JWT_ACCESS_EXPIRY` | Access Token 만료 시간 (초) | `1800` (30분) | 공통 |
| `JWT_REFRESH_EXPIRY` | Refresh Token 만료 시간 (초) | `604800` (7일) | 공통 |
| `ORACLE_URL` | Oracle JDBC 연결 URL | `jdbc:oracle:thin:@oracle-db:1521/ORCL` | `prod` |
| `ORACLE_USER` | Oracle 접속 계정명 | `auth_user` | `prod` |
| `ORACLE_PASSWORD` | Oracle 접속 비밀번호 | (운영 환경에 맞게 필수 지정) | `prod` |

## API 문서 (API Documentation)

`dev` 프로파일로 실행 시 Swagger UI를 통해 API 문서를 확인하고 테스트할 수 있습니다.
*   **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
*   **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

> **참고:** 보안을 위해 `prod` 프로파일에서는 Swagger UI가 비활성화되어 있습니다.
